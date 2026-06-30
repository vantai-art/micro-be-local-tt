package com.rainbowforest.hrmservice.service.impl;

import com.rainbowforest.hrmservice.domain.Employee;
import com.rainbowforest.hrmservice.domain.Payroll;
import com.rainbowforest.hrmservice.dto.PayrollDto;
import com.rainbowforest.hrmservice.enums.AttendanceStatus;
import com.rainbowforest.hrmservice.enums.EmployeeStatus;
import com.rainbowforest.hrmservice.enums.PayrollStatus;
import com.rainbowforest.hrmservice.repository.AttendanceRepository;
import com.rainbowforest.hrmservice.repository.EmployeeRepository;
import com.rainbowforest.hrmservice.repository.PayrollRepository;
import com.rainbowforest.hrmservice.enums.ActionType;
import com.rainbowforest.hrmservice.service.ActivityLogService;
import com.rainbowforest.hrmservice.service.PayrollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final ActivityLogService activityLogService;

    // Tỷ lệ BHXH người lao động: 8%
    private static final BigDecimal SOCIAL_INSURANCE_RATE = new BigDecimal("0.08");
    // Tỷ lệ BHYT người lao động: 1.5%
    private static final BigDecimal HEALTH_INSURANCE_RATE = new BigDecimal("0.015");
    // Hệ số lương overtime: 1.5x
    private static final BigDecimal OVERTIME_RATE = new BigDecimal("1.5");
    // Ngưỡng chịu thuế TNCN (9 triệu)
    private static final BigDecimal PIT_THRESHOLD = new BigDecimal("9000000");
    // Ngày công chuẩn mỗi tháng
    private static final int STANDARD_DAYS = 26;

    @Override
    @Transactional
    public PayrollDto.Response calculate(PayrollDto.CalculateRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nhân viên id: " + request.getEmployeeId()));

        // Tính khoảng thời gian trong tháng
        LocalDate from = LocalDate.of(request.getYear(), request.getMonth(), 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        // Đếm ngày công thực tế (PRESENT + LATE + ON_LEAVE)
        long presentDays = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                request.getEmployeeId(), from, to, AttendanceStatus.PRESENT);
        long lateDays = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                request.getEmployeeId(), from, to, AttendanceStatus.LATE);
        long leaveDays = attendanceRepository.countByEmployeeAndDateRangeAndStatus(
                request.getEmployeeId(), from, to, AttendanceStatus.ON_LEAVE);
        int actualDays = (int) (presentDays + lateDays);
        int paidLeaveDays = (int) leaveDays;

        // Giờ làm thêm
        Double overtimeHoursRaw = attendanceRepository.sumOvertimeHoursByEmployeeAndDateRange(
                request.getEmployeeId(), from, to);
        double overtimeHours = overtimeHoursRaw != null ? overtimeHoursRaw : 0.0;

        BigDecimal basicSalary = employee.getBasicSalary() != null ? employee.getBasicSalary() : BigDecimal.ZERO;
        BigDecimal allowance = employee.getAllowance() != null ? employee.getAllowance() : BigDecimal.ZERO;
        BigDecimal bonus = request.getBonus() != null ? request.getBonus() : BigDecimal.ZERO;
        BigDecimal otherDeductions = request.getOtherDeductions() != null ? request.getOtherDeductions() : BigDecimal.ZERO;

        // Lương theo ngày công thực tế + phép được tính lương
        BigDecimal earnedSalary = basicSalary
                .multiply(BigDecimal.valueOf(actualDays + paidLeaveDays))
                .divide(BigDecimal.valueOf(STANDARD_DAYS), 2, RoundingMode.HALF_UP);

        // Lương overtime = (basicSalary / 26 / 8) * 1.5 * overtimeHours
        BigDecimal hourlyRate = basicSalary.divide(BigDecimal.valueOf(STANDARD_DAYS * 8L), 4, RoundingMode.HALF_UP);
        BigDecimal overtimePay = hourlyRate
                .multiply(OVERTIME_RATE)
                .multiply(BigDecimal.valueOf(overtimeHours))
                .setScale(2, RoundingMode.HALF_UP);

        // BHXH = 8% * basicSalary
        BigDecimal socialInsurance = basicSalary.multiply(SOCIAL_INSURANCE_RATE).setScale(2, RoundingMode.HALF_UP);
        // BHYT = 1.5% * basicSalary
        BigDecimal healthInsurance = basicSalary.multiply(HEALTH_INSURANCE_RATE).setScale(2, RoundingMode.HALF_UP);

        // Thu nhập chịu thuế = earnedSalary + overtimePay + bonus + allowance - BHXH - BHYT
        BigDecimal taxableIncome = earnedSalary.add(overtimePay).add(bonus).add(allowance)
                .subtract(socialInsurance).subtract(healthInsurance);

        // Thuế TNCN đơn giản (thu nhập > 9 triệu, thuế 10%)
        BigDecimal pit = BigDecimal.ZERO;
        if (taxableIncome.compareTo(PIT_THRESHOLD) > 0) {
            pit = taxableIncome.subtract(PIT_THRESHOLD)
                    .multiply(new BigDecimal("0.10"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Lương thực nhận
        BigDecimal netSalary = taxableIncome.subtract(pit).subtract(otherDeductions);

        // Lưu hoặc cập nhật
        Payroll payroll = payrollRepository
                .findByEmployeeIdAndPayMonthAndPayYear(request.getEmployeeId(), request.getMonth(), request.getYear())
                .orElse(Payroll.builder().employee(employee).payMonth(request.getMonth()).payYear(request.getYear()).build());

        payroll.setStandardWorkingDays(STANDARD_DAYS);
        payroll.setActualWorkingDays(actualDays);
        payroll.setPaidLeaveDays(paidLeaveDays);
        payroll.setOvertimeHours(overtimeHours);
        payroll.setBasicSalary(basicSalary);
        payroll.setAllowance(allowance);
        payroll.setOvertimePay(overtimePay);
        payroll.setBonus(bonus);
        payroll.setSocialInsurance(socialInsurance);
        payroll.setHealthInsurance(healthInsurance);
        payroll.setPersonalIncomeTax(pit);
        payroll.setOtherDeductions(otherDeductions);
        payroll.setNetSalary(netSalary);
        payroll.setStatus(PayrollStatus.CALCULATED);
        payroll.setNote(request.getNote());

        log.info("Tính lương nhân viên {} tháng {}/{}: net={}",
                employee.getEmployeeCode(), request.getMonth(), request.getYear(), netSalary);

        PayrollDto.Response result = toResponse(payrollRepository.save(payroll));
        activityLogService.log(ActionType.CALCULATE_PAYROLL,
                "PAYROLL", String.valueOf(result.getId()),
                employee.getFullName() + " (" + employee.getEmployeeCode() + ")",
                null, String.format("Tháng %d/%d | Net: %s VNĐ",
                        request.getMonth(), request.getYear(), netSalary.toPlainString()),
                request.getNote(), null);
        return result;
    }

    @Override
    @Transactional
    public PayrollDto.Response calculateAll(int month, int year) {
        // Tính lương hàng loạt — trả về bản ghi cuối cùng (thường dùng trong batch job)
        List<Employee> employees = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);
        Payroll last = null;
        for (Employee e : employees) {
            PayrollDto.CalculateRequest req = PayrollDto.CalculateRequest.builder()
                    .employeeId(e.getId())
                    .month(month)
                    .year(year)
                    .bonus(BigDecimal.ZERO)
                    .otherDeductions(BigDecimal.ZERO)
                    .build();
            PayrollDto.Response res = calculate(req);
            last = payrollRepository.findById(res.getId()).orElse(null);
        }
        log.info("Tính lương hàng loạt tháng {}/{}: {} nhân viên", month, year, employees.size());
        return last != null ? toResponse(last) : null;
    }

    @Override
    public PayrollDto.Response getById(Long id) {
        return toResponse(findPayroll(id));
    }

    @Override
    public List<PayrollDto.Response> getByEmployee(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PayrollDto.Response> getByMonthAndYear(int month, int year) {
        return payrollRepository.findByPayMonthAndPayYear(month, year)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PayrollDto.Response approve(Long id, Long approverId) {
        Payroll payroll = findPayroll(id);
        if (payroll.getStatus() != PayrollStatus.CALCULATED) {
            throw new IllegalStateException("Chỉ duyệt bảng lương đã tính toán");
        }
        payroll.setStatus(PayrollStatus.APPROVED);
        payroll.setApprovedBy(approverId);
        payroll.setApprovedAt(LocalDateTime.now());
        PayrollDto.Response r1 = toResponse(payrollRepository.save(payroll));
        activityLogService.log(ActionType.APPROVE_PAYROLL,
                "PAYROLL", String.valueOf(id),
                payroll.getEmployee().getFullName() + " (" + payroll.getEmployee().getEmployeeCode() + ")",
                "CALCULATED", "APPROVED", null, null);
        return r1;
    }

    @Override
    @Transactional
    public PayrollDto.Response markPaid(Long id) {
        Payroll payroll = findPayroll(id);
        if (payroll.getStatus() != PayrollStatus.APPROVED) {
            throw new IllegalStateException("Chỉ thanh toán bảng lương đã được duyệt");
        }
        payroll.setStatus(PayrollStatus.PAID);
        payroll.setPaidAt(LocalDateTime.now());
        PayrollDto.Response r2 = toResponse(payrollRepository.save(payroll));
        activityLogService.log(ActionType.MARK_PAID_PAYROLL,
                "PAYROLL", String.valueOf(id),
                payroll.getEmployee().getFullName() + " (" + payroll.getEmployee().getEmployeeCode() + ")",
                "APPROVED", "PAID | " + payroll.getNetSalary().toPlainString() + " VNĐ", null, null);
        return r2;
    }

    @Override
    public BigDecimal getTotalPayrollByMonthAndYear(int month, int year) {
        return payrollRepository.sumNetSalaryByMonthAndYear(month, year);
    }

    // ---- Helpers ----

    private Payroll findPayroll(Long id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bảng lương id: " + id));
    }

    private PayrollDto.Response toResponse(Payroll p) {
        Employee e = p.getEmployee();
        return PayrollDto.Response.builder()
                .id(p.getId())
                .employeeId(e.getId())
                .employeeName(e.getFullName())
                .employeeCode(e.getEmployeeCode())
                .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                .positionName(e.getPosition() != null ? e.getPosition().getName() : null)
                .payMonth(p.getPayMonth())
                .payYear(p.getPayYear())
                .standardWorkingDays(p.getStandardWorkingDays())
                .actualWorkingDays(p.getActualWorkingDays())
                .paidLeaveDays(p.getPaidLeaveDays())
                .overtimeHours(p.getOvertimeHours())
                .basicSalary(p.getBasicSalary())
                .allowance(p.getAllowance())
                .overtimePay(p.getOvertimePay())
                .bonus(p.getBonus())
                .socialInsurance(p.getSocialInsurance())
                .healthInsurance(p.getHealthInsurance())
                .personalIncomeTax(p.getPersonalIncomeTax())
                .otherDeductions(p.getOtherDeductions())
                .netSalary(p.getNetSalary())
                .status(p.getStatus())
                .note(p.getNote())
                .approvedBy(p.getApprovedBy())
                .approvedAt(p.getApprovedAt())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
