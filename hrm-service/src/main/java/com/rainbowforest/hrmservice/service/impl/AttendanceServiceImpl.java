package com.rainbowforest.hrmservice.service.impl;

import com.rainbowforest.hrmservice.domain.Attendance;
import com.rainbowforest.hrmservice.domain.Branch;
import com.rainbowforest.hrmservice.domain.Employee;
import com.rainbowforest.hrmservice.domain.Shift;
import com.rainbowforest.hrmservice.dto.AttendanceDto;
import com.rainbowforest.hrmservice.enums.ActionType;
import com.rainbowforest.hrmservice.enums.AttendanceStatus;
import com.rainbowforest.hrmservice.repository.AttendanceRepository;
import com.rainbowforest.hrmservice.repository.EmployeeRepository;
import com.rainbowforest.hrmservice.repository.ShiftRepository;
import com.rainbowforest.hrmservice.service.ActivityLogService;
import com.rainbowforest.hrmservice.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final ActivityLogService activityLogService;

    // Giờ bắt đầu làm việc chuẩn: 08:00
    private static final LocalTime STANDARD_START = LocalTime.of(8, 0);
    // Giờ kết thúc làm việc chuẩn: 17:30
    private static final LocalTime STANDARD_END = LocalTime.of(17, 30);
    // Số phút trễ được phép
    private static final int LATE_THRESHOLD_MINUTES = 15;

    private static final double EARTH_RADIUS_M = 6_371_000.0;

    @Override
    @Transactional
    public AttendanceDto.Response checkIn(Long employeeId) {
        Employee employee = findEmployee(employeeId);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElse(Attendance.builder()
                        .employee(employee)
                        .attendanceDate(today)
                        .overtimeHours(0.0)
                        .build());

        attendance.setCheckIn(now);
        attendance.setStatus(now.isAfter(STANDARD_START.plusMinutes(LATE_THRESHOLD_MINUTES))
                ? AttendanceStatus.LATE
                : AttendanceStatus.PRESENT);

        AttendanceDto.Response result = toResponse(attendanceRepository.save(attendance));
        activityLogService.log(ActionType.CHECK_IN,
                "ATTENDANCE", employee.getEmployeeCode(),
                employee.getFullName() + " (" + employee.getEmployeeCode() + ")",
                null, "Check-in lúc " + now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                null, null);
        return result;
    }

    @Override
    @Transactional
    public AttendanceDto.Response checkInWithGeo(AttendanceDto.CheckInRequest request) {
        Employee employee = findEmployee(request.getEmployeeId());
        Branch branch = employee.getBranch();

        if (branch == null) {
            throw new IllegalStateException("Nhân viên chưa được gán chi nhánh. Vui lòng liên hệ quản trị viên.");
        }

        LocalTime now = LocalTime.now();

        // --- Kiểm tra GPS ---
        if (branch.getLatitude() != null && branch.getLongitude() != null
                && branch.getRadiusMeters() != null
                && request.getLatitude() != null && request.getLongitude() != null) {
            double dist = haversineDistance(
                    request.getLatitude(), request.getLongitude(),
                    branch.getLatitude(), branch.getLongitude());
            if (dist > branch.getRadiusMeters()) {
                throw new IllegalStateException(String.format(
                        "Vị trí hiện tại (%.0f m) nằm ngoài phạm vi chi nhánh (%.0f m). Không thể chấm công.",
                        dist, branch.getRadiusMeters()));
            }
        }

        // --- Kiểm tra WiFi ---
        boolean isDemo = Boolean.TRUE.equals(branch.getIsDemo());
        if (isDemo) {
            // Chế độ demo: chỉ kiểm tra SSID
            if (branch.getSsid() != null && !branch.getSsid().isBlank()) {
                if (request.getSsid() == null || !branch.getSsid().equalsIgnoreCase(request.getSsid().trim())) {
                    throw new IllegalStateException(
                            "Vui lòng kết nối WiFi \"" + branch.getSsid() + "\" của chi nhánh trước khi chấm công.");
                }
            }
        } else {
            // Chế độ production: kiểm tra BSSID (MAC address)
            if (branch.getBssid() != null && !branch.getBssid().isBlank()) {
                if (request.getBssid() == null || !branch.getBssid().equalsIgnoreCase(request.getBssid().trim())) {
                    throw new IllegalStateException(
                            "Thiết bị không kết nối đúng điểm phát WiFi của chi nhánh. Không thể chấm công.");
                }
            }
        }

        // --- Xác định ca làm việc ---
        Shift matchedShift = findMatchingShift(branch.getId(), now);
        LocalTime shiftStart = matchedShift != null ? matchedShift.getStartTime() : STANDARD_START;

        // --- Tạo/cập nhật bản ghi chấm công ---
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(request.getEmployeeId(), today)
                .orElse(Attendance.builder()
                        .employee(employee)
                        .attendanceDate(today)
                        .overtimeHours(0.0)
                        .build());

        attendance.setCheckIn(now);
        attendance.setCheckInLatitude(request.getLatitude());
        attendance.setCheckInLongitude(request.getLongitude());
        attendance.setBssidUsed(request.getBssid());
        attendance.setIsDemo(isDemo);
        attendance.setShift(matchedShift);
        attendance.setStatus(now.isAfter(shiftStart.plusMinutes(LATE_THRESHOLD_MINUTES))
                ? AttendanceStatus.LATE
                : AttendanceStatus.PRESENT);

        AttendanceDto.Response result = toResponse(attendanceRepository.save(attendance));
        activityLogService.log(ActionType.CHECK_IN,
                "ATTENDANCE", employee.getEmployeeCode(),
                employee.getFullName() + " (" + employee.getEmployeeCode() + ")",
                null, String.format("Check-in lúc %s | GPS: %.5f,%.5f | %s",
                        now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                        request.getLatitude() != null ? request.getLatitude() : 0.0,
                        request.getLongitude() != null ? request.getLongitude() : 0.0,
                        isDemo ? "Demo" : "Production"),
                null, null);
        return result;
    }

    @Override
    @Transactional
    public AttendanceDto.Response checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();

        // Tìm hôm nay trước
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElse(null);

        // Nếu hôm nay không có → tìm hôm qua (ca đêm), chỉ lấy nếu chưa check-out
        if (attendance == null) {
            attendance = attendanceRepository
                    .findByEmployeeIdAndAttendanceDate(employeeId, today.minusDays(1))
                    .filter(a -> a.getCheckOut() == null)
                    .orElse(null);
        }

        if (attendance == null) {
            throw new IllegalStateException("Chưa check-in hôm nay hoặc hôm qua");
        }

        LocalTime now = LocalTime.now();
        attendance.setCheckOut(now);

        if (attendance.getCheckIn() != null) {
            long minutes;
            if (now.isBefore(attendance.getCheckIn())) {
                minutes = (24 * 60) - attendance.getCheckIn().toSecondOfDay() / 60 + now.toSecondOfDay() / 60;
            } else {
                minutes = ChronoUnit.MINUTES.between(attendance.getCheckIn(), now);
            }
            double worked = Math.max(0, (minutes - 60) / 60.0);
            attendance.setWorkedHours(Math.round(worked * 100.0) / 100.0);

            LocalTime shiftEnd = attendance.getShift() != null ? attendance.getShift().getEndTime() : STANDARD_END;
            if (now.isAfter(shiftEnd)) {
                double overtime = ChronoUnit.MINUTES.between(shiftEnd, now) / 60.0;
                attendance.setOvertimeHours(Math.round(overtime * 100.0) / 100.0);
            }
        }

        AttendanceDto.Response result = toResponse(attendanceRepository.save(attendance));
        Employee emp = attendance.getEmployee();
        activityLogService.log(ActionType.CHECK_OUT,
                "ATTENDANCE", emp.getEmployeeCode(),
                emp.getFullName() + " (" + emp.getEmployeeCode() + ")",
                null, String.format("Check-out lúc %s | Làm: %.1fh",
                        now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                        attendance.getWorkedHours() != null ? attendance.getWorkedHours() : 0.0),
                null, null);
        return result;
    }

    @Override
    @Transactional
    public AttendanceDto.Response createOrUpdate(AttendanceDto.Request request) {
        Employee employee = findEmployee(request.getEmployeeId());

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(request.getEmployeeId(), request.getAttendanceDate())
                .orElse(Attendance.builder()
                        .employee(employee)
                        .attendanceDate(request.getAttendanceDate())
                        .build());

        if (request.getCheckIn() != null)
            attendance.setCheckIn(request.getCheckIn());
        if (request.getCheckOut() != null)
            attendance.setCheckOut(request.getCheckOut());
        if (request.getStatus() != null)
            attendance.setStatus(request.getStatus());
        if (request.getNote() != null)
            attendance.setNote(request.getNote());
        if (request.getOvertimeHours() != null)
            attendance.setOvertimeHours(request.getOvertimeHours());

        // Tự tính giờ làm nếu có cả checkin và checkout
        if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
            long minutes = ChronoUnit.MINUTES.between(attendance.getCheckIn(), attendance.getCheckOut());
            attendance.setWorkedHours(Math.max(0, (minutes - 60) / 60.0));
        }

        return toResponse(attendanceRepository.save(attendance));
    }

    @Override
    public AttendanceDto.Response getById(Long id) {
        return toResponse(attendanceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chấm công id: " + id)));
    }

    @Override
    public List<AttendanceDto.Response> getByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to) {
        return attendanceRepository.findByEmployeeIdAndAttendanceDateBetween(employeeId, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDto.Response> getByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDate(date)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public AttendanceDto.MonthlySummary getMonthlySummary(Long employeeId, int month, int year) {
        Employee employee = findEmployee(employeeId);
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        long present = attendanceRepository.countByEmployeeAndDateRangeAndStatus(employeeId, from, to,
                AttendanceStatus.PRESENT);
        long absent = attendanceRepository.countByEmployeeAndDateRangeAndStatus(employeeId, from, to,
                AttendanceStatus.ABSENT);
        long late = attendanceRepository.countByEmployeeAndDateRangeAndStatus(employeeId, from, to,
                AttendanceStatus.LATE);
        long halfDay = attendanceRepository.countByEmployeeAndDateRangeAndStatus(employeeId, from, to,
                AttendanceStatus.HALF_DAY);
        long onLeave = attendanceRepository.countByEmployeeAndDateRangeAndStatus(employeeId, from, to,
                AttendanceStatus.ON_LEAVE);
        Double workedHours = attendanceRepository.sumWorkedHoursByEmployeeAndDateRange(employeeId, from, to);
        Double overtimeHours = attendanceRepository.sumOvertimeHoursByEmployeeAndDateRange(employeeId, from, to);

        return AttendanceDto.MonthlySummary.builder()
                .employeeId(employeeId)
                .employeeName(employee.getFullName())
                .month(month)
                .year(year)
                .presentDays(present + late) // late vẫn tính là có mặt
                .absentDays(absent)
                .lateDays(late)
                .halfDays(halfDay)
                .leaveDays(onLeave)
                .totalWorkedHours(workedHours != null ? workedHours : 0)
                .totalOvertimeHours(overtimeHours != null ? overtimeHours : 0)
                .build();
    }

    @Override
    @Transactional
    public AttendanceDto.Response confirm(Long id, Long confirmedBy) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chấm công id: " + id));
        attendance.setConfirmedBy(confirmedBy);
        return toResponse(attendanceRepository.save(attendance));
    }

    // ---- Helpers ----

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nhân viên id: " + id));
    }

    // Tìm ca làm việc phù hợp với giờ hiện tại (cho phép sai lệch 30 phút trước ca)
    private Shift findMatchingShift(Long branchId, LocalTime now) {
        return shiftRepository.findByBranchId(branchId).stream()
                .filter(s -> !now.isBefore(s.getStartTime().minusMinutes(30))
                        && !now.isAfter(s.getEndTime()))
                .findFirst()
                .orElse(null);
    }

    // Haversine formula — trả về khoảng cách tính bằng mét
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_M * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private AttendanceDto.Response toResponse(Attendance a) {
        return AttendanceDto.Response.builder()
                .id(a.getId())
                .employeeId(a.getEmployee().getId())
                .employeeName(a.getEmployee().getFullName())
                .employeeCode(a.getEmployee().getEmployeeCode())
                .attendanceDate(a.getAttendanceDate())
                .checkIn(a.getCheckIn())
                .checkOut(a.getCheckOut())
                .workedHours(a.getWorkedHours())
                .overtimeHours(a.getOvertimeHours())
                .status(a.getStatus())
                .note(a.getNote())
                .confirmedBy(a.getConfirmedBy())
                .shiftId(a.getShift() != null ? a.getShift().getId() : null)
                .shiftName(a.getShift() != null ? a.getShift().getName() : null)
                .checkInLatitude(a.getCheckInLatitude())
                .checkInLongitude(a.getCheckInLongitude())
                .bssidUsed(a.getBssidUsed())
                .isDemo(a.getIsDemo())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
