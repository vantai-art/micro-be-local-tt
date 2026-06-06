package com.rainbowforest.hrmservice.controller;

import com.rainbowforest.hrmservice.dto.AttendanceDto;
import com.rainbowforest.hrmservice.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/hrm/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // Check-in nhanh (dùng thời gian hệ thống)
    @PostMapping("/check-in/{employeeId}")
    public ResponseEntity<AttendanceDto.Response> checkIn(@PathVariable Long employeeId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.checkIn(employeeId));
    }

    // Check-out nhanh
    @PatchMapping("/check-out/{employeeId}")
    public ResponseEntity<AttendanceDto.Response> checkOut(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.checkOut(employeeId));
    }

    // Tạo/Cập nhật chấm công thủ công (admin)
    @PostMapping
    public ResponseEntity<AttendanceDto.Response> createOrUpdate(@Valid @RequestBody AttendanceDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.createOrUpdate(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceDto.Response>> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getByEmployeeAndDateRange(employeeId, from, to));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<AttendanceDto.Response>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getByDate(date));
    }

    @GetMapping("/employee/{employeeId}/summary")
    public ResponseEntity<AttendanceDto.MonthlySummary> getMonthlySummary(
            @PathVariable Long employeeId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.getMonthlySummary(employeeId, month, year));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AttendanceDto.Response> confirm(
            @PathVariable Long id,
            @RequestParam Long confirmedBy) {
        return ResponseEntity.ok(attendanceService.confirm(id, confirmedBy));
    }
}
