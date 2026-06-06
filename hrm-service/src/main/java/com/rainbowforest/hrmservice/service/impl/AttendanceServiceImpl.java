package com.rainbowforest.hrmservice.service.impl;

import com.rainbowforest.hrmservice.domain.Attendance;
import com.rainbowforest.hrmservice.domain.Employee;
import com.rainbowforest.hrmservice.dto.AttendanceDto;
import com.rainbowforest.hrmservice.enums.AttendanceStatus;
import com.rainbowforest.hrmservice.repository.AttendanceRepository;
import com.rainbowforest.hrmservice.repository.EmployeeRepository;
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

    // Giờ bắt đầu làm việc chuẩn: 08:00
    private static final LocalTime STANDARD_START = LocalTime.of(8, 0);
    // Giờ kết thúc làm việc chuẩn: 17:30
    private static final LocalTime STANDARD_END = LocalTime.of(17, 30);
    // Số phút trễ được phép
    private static final int LATE_THRESHOLD_MINUTES = 15;

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
                ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);

        return toResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public AttendanceDto.Response checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new IllegalStateException("Chưa check-in hôm nay"));

        LocalTime now = LocalTime.now();
        attendance.setCheckOut(now);

        if (attendance.getCheckIn() != null) {
            long minutes = ChronoUnit.MINUTES.between(attendance.getCheckIn(), now);
            // Trừ 1 tiếng nghỉ trưa
            double worked = Math.max(0, (minutes - 60) / 60.0);
            attendance.setWorkedHours(Math.round(worked * 100.0) / 100.0);

            // Tính overtime nếu checkout sau 17:30
            if (now.isAfter(STANDARD_END)) {
                double overtime = ChronoUnit.MINUTES.between(STANDARD_END, now) / 60.0;
                attendance.setOvertimeHours(Math.round(overtime * 100.0) / 100.0);
            }
        }

        return toResponse(attendanceRepository.save(attendance));
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

        if (request.getCheckIn() != null) attendance.setCheckIn(request.getCheckIn());
        if (request.getCheckOut() != null) attendance.setCheckOut(request.getCheckOut());
        if (request.getStatus() != null) attendance.setStatus(request.getStatus());
        if (request.getNote() != null) attendance.setNote(request.getNote());
        if (request.getOvertimeHours() != null) attendance.setOvertimeHours(request.getOvertimeHours());

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

        long present = attendanceRepository.countByEmployeeAndDateRangeAndStatus(employeeId, from, to, AttendanceStatus.PRESENT);
        long absent = attendanceRepository.countByEmployeeAndDateRangeAndStatus(employeeId, from, to, AttendanceStatus.ABSENT);
        long late = attendanceRepository.countByEmployeeAndDateRangeAndStatus(employeeId, from, to, AttendanceStatus.LATE);
        long halfDay = attendanceRepository.countByEmployeeAndDateRangeAndStatus(employeeId, from, to, AttendanceStatus.HALF_DAY);
        long onLeave = attendanceRepository.countByEmployeeAndDateRangeAndStatus(employeeId, from, to, AttendanceStatus.ON_LEAVE);
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
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
