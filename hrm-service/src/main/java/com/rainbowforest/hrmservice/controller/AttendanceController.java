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
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // FE gọi GET /hrm/attendance
    @GetMapping("/hrm/attendance")
    public ResponseEntity<List<AttendanceDto.Response>> getAllAttendance() {
        return ResponseEntity.ok(attendanceService.getByDate(LocalDate.now()));
    }

    // FE gọi POST /hrm/attendance/check-in body: {employeeId, latitude, longitude, bssid, ssid}
    @PostMapping("/hrm/attendance/check-in")
    public ResponseEntity<AttendanceDto.Response> checkIn(@RequestBody AttendanceDto.CheckInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.checkInWithGeo(request));
    }

    // FE gọi POST /hrm/attendance/check-out body: {attendanceId}
    @PostMapping("/hrm/attendance/check-out")
    public ResponseEntity<AttendanceDto.Response> checkOut(@RequestBody Map<String, Object> body) {
        Long employeeId = Long.parseLong(body.get("attendanceId").toString());
        return ResponseEntity.ok(attendanceService.checkOut(employeeId));
    }

    // ── Legacy endpoints (giữ lại) ────────────────────────────────────────────
    @PostMapping("/hrm/attendances/check-in/{employeeId}")
    public ResponseEntity<AttendanceDto.Response> checkInLegacy(@PathVariable Long employeeId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.checkIn(employeeId));
    }

    @PatchMapping("/hrm/attendances/check-out/{employeeId}")
    public ResponseEntity<AttendanceDto.Response> checkOutLegacy(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.checkOut(employeeId));
    }

    @PostMapping("/hrm/attendances")
    public ResponseEntity<AttendanceDto.Response> createOrUpdate(@Valid @RequestBody AttendanceDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.createOrUpdate(request));
    }

    @GetMapping("/hrm/attendances/{id}")
    public ResponseEntity<AttendanceDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getById(id));
    }

    @GetMapping("/hrm/attendances/employee/{employeeId}")
    public ResponseEntity<List<AttendanceDto.Response>> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getByEmployeeAndDateRange(employeeId, from, to));
    }

    @GetMapping("/hrm/attendances/date/{date}")
    public ResponseEntity<List<AttendanceDto.Response>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getByDate(date));
    }

    @GetMapping("/hrm/attendances/employee/{employeeId}/summary")
    public ResponseEntity<AttendanceDto.MonthlySummary> getMonthlySummary(
            @PathVariable Long employeeId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.getMonthlySummary(employeeId, month, year));
    }

    @PatchMapping("/hrm/attendances/{id}/confirm")
    public ResponseEntity<AttendanceDto.Response> confirm(
            @PathVariable Long id,
            @RequestParam Long confirmedBy) {
        return ResponseEntity.ok(attendanceService.confirm(id, confirmedBy));
    }
}
