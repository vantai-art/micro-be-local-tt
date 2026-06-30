package com.rainbowforest.hrmservice.service;

import com.rainbowforest.hrmservice.dto.AttendanceDto;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceDto.Response checkIn(Long employeeId);
    AttendanceDto.Response checkInWithGeo(AttendanceDto.CheckInRequest request);
    AttendanceDto.Response checkOut(Long employeeId);
    AttendanceDto.Response createOrUpdate(AttendanceDto.Request request);
    AttendanceDto.Response getById(Long id);
    List<AttendanceDto.Response> getByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to);
    List<AttendanceDto.Response> getByDate(LocalDate date);
    AttendanceDto.MonthlySummary getMonthlySummary(Long employeeId, int month, int year);
    AttendanceDto.Response confirm(Long id, Long confirmedBy);
}
