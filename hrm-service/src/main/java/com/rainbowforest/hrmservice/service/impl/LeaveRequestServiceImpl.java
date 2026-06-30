package com.rainbowforest.hrmservice.service.impl;

import com.rainbowforest.hrmservice.domain.Employee;
import com.rainbowforest.hrmservice.domain.LeaveRequest;
import com.rainbowforest.hrmservice.dto.LeaveRequestDto;
import com.rainbowforest.hrmservice.enums.AttendanceStatus;
import com.rainbowforest.hrmservice.enums.LeaveStatus;
import com.rainbowforest.hrmservice.enums.LeaveType;
import com.rainbowforest.hrmservice.repository.AttendanceRepository;
import com.rainbowforest.hrmservice.repository.EmployeeRepository;
import com.rainbowforest.hrmservice.repository.LeaveRequestRepository;
import com.rainbowforest.hrmservice.enums.ActionType;
import com.rainbowforest.hrmservice.service.ActivityLogService;
import com.rainbowforest.hrmservice.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public LeaveRequestDto.Response create(LeaveRequestDto.Request request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        Employee employee = findEmployee(request.getEmployeeId());

        // Kiểm tra trùng lịch
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
                request.getEmployeeId(), request.getStartDate(), request.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Đã có đơn nghỉ phép trùng lịch trong khoảng thời gian này");
        }

        int totalDays = countWorkingDays(request.getStartDate(), request.getEndDate());

        // Kiểm tra số ngày phép còn lại (chỉ áp dụng cho nghỉ phép năm)
        if (request.getLeaveType() == LeaveType.ANNUAL) {
            int usedDays = leaveRequestRepository.sumApprovedDaysByTypeAndYear(
                    request.getEmployeeId(), LeaveType.ANNUAL, request.getStartDate().getYear());
            int remaining = employee.getAnnualLeaveDays() - usedDays;
            if (totalDays > remaining) {
                throw new IllegalStateException(
                        String.format("Không đủ ngày phép. Còn lại: %d ngày, yêu cầu: %d ngày", remaining, totalDays));
            }
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequestDto.Response result = toResponse(leaveRequestRepository.save(leaveRequest));
        activityLogService.log(ActionType.CREATE_LEAVE_REQUEST,
                "LEAVE_REQUEST", String.valueOf(result.getId()),
                employee.getFullName() + " (" + employee.getEmployeeCode() + ")",
                null, String.format("%s: %s → %s (%d ngày)",
                        request.getLeaveType(), request.getStartDate(), request.getEndDate(), totalDays),
                request.getReason(), null);
        return result;
    }

    @Override
    public LeaveRequestDto.Response getById(Long id) {
        return toResponse(findLeaveRequest(id));
    }

    @Override
    public List<LeaveRequestDto.Response> getByEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDto.Response> getByStatus(LeaveStatus status) {
        return leaveRequestRepository.findByStatus(status)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDto.Response> getAll() {
        return leaveRequestRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveRequestDto.Response approve(Long id, Long approverId) {
        LeaveRequest lr = findLeaveRequest(id);
        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể duyệt đơn đang chờ duyệt");
        }

        lr.setStatus(LeaveStatus.APPROVED);
        lr.setApprovedBy(approverId);
        lr.setApprovedAt(LocalDateTime.now());

        // Tự động tạo attendance ON_LEAVE cho từng ngày làm việc
        createAttendanceForLeave(lr);

        log.info("Duyệt đơn nghỉ phép id={} cho nhân viên id={}", id, lr.getEmployee().getId());
        LeaveRequestDto.Response result = toResponse(leaveRequestRepository.save(lr));
        activityLogService.log(ActionType.APPROVE_LEAVE,
                "LEAVE_REQUEST", String.valueOf(id),
                lr.getEmployee().getFullName() + " (" + lr.getEmployee().getEmployeeCode() + ")",
                "PENDING", "APPROVED",
                null, null);
        return result;
    }

    @Override
    @Transactional
    public LeaveRequestDto.Response reject(Long id, Long approverId, String reason) {
        LeaveRequest lr = findLeaveRequest(id);
        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể từ chối đơn đang chờ duyệt");
        }

        lr.setStatus(LeaveStatus.REJECTED);
        lr.setApprovedBy(approverId);
        lr.setApprovedAt(LocalDateTime.now());
        lr.setRejectReason(reason);

        LeaveRequestDto.Response result = toResponse(leaveRequestRepository.save(lr));
        activityLogService.log(ActionType.REJECT_LEAVE,
                "LEAVE_REQUEST", String.valueOf(id),
                lr.getEmployee().getFullName() + " (" + lr.getEmployee().getEmployeeCode() + ")",
                "PENDING", "REJECTED", reason, null);
        return result;
    }

    @Override
    @Transactional
    public LeaveRequestDto.Response cancel(Long id) {
        LeaveRequest lr = findLeaveRequest(id);
        if (lr.getStatus() == LeaveStatus.PAID || lr.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Không thể hủy đơn đã bắt đầu hoặc đã thanh toán");
        }
        lr.setStatus(LeaveStatus.CANCELLED);
        LeaveRequestDto.Response result = toResponse(leaveRequestRepository.save(lr));
        activityLogService.log(ActionType.CANCEL_LEAVE,
                "LEAVE_REQUEST", String.valueOf(id),
                lr.getEmployee().getFullName() + " (" + lr.getEmployee().getEmployeeCode() + ")",
                lr.getStatus().name(), "CANCELLED", null, null);
        return result;
    }

    // ---- Helpers ----

    private void createAttendanceForLeave(LeaveRequest lr) {
        LocalDate date = lr.getStartDate();
        while (!date.isAfter(lr.getEndDate())) {
            if (!isWeekend(date)) {
                LocalDate finalDate = date;
                attendanceRepository.findByEmployeeIdAndAttendanceDate(lr.getEmployee().getId(), date)
                        .ifPresentOrElse(
                                a -> { a.setStatus(AttendanceStatus.ON_LEAVE); attendanceRepository.save(a); },
                                () -> attendanceRepository.save(
                                        com.rainbowforest.hrmservice.domain.Attendance.builder()
                                                .employee(lr.getEmployee())
                                                .attendanceDate(finalDate)
                                                .status(AttendanceStatus.ON_LEAVE)
                                                .overtimeHours(0.0)
                                                .note("Nghỉ phép: " + lr.getLeaveType())
                                                .build()
                                )
                        );
            }
            date = date.plusDays(1);
        }
    }

    private int countWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            if (!isWeekend(date)) count++;
            date = date.plusDays(1);
        }
        return count;
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nhân viên id: " + id));
    }

    private LeaveRequest findLeaveRequest(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn nghỉ phép id: " + id));
    }

    private LeaveRequestDto.Response toResponse(LeaveRequest lr) {
        return LeaveRequestDto.Response.builder()
                .id(lr.getId())
                .employeeId(lr.getEmployee().getId())
                .employeeName(lr.getEmployee().getFullName())
                .employeeCode(lr.getEmployee().getEmployeeCode())
                .departmentName(lr.getEmployee().getDepartment() != null
                        ? lr.getEmployee().getDepartment().getName() : null)
                .leaveType(lr.getLeaveType())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .totalDays(lr.getTotalDays())
                .reason(lr.getReason())
                .status(lr.getStatus())
                .approvedBy(lr.getApprovedBy())
                .approvedAt(lr.getApprovedAt())
                .rejectReason(lr.getRejectReason())
                .createdAt(lr.getCreatedAt())
                .updatedAt(lr.getUpdatedAt())
                .build();
    }
}
