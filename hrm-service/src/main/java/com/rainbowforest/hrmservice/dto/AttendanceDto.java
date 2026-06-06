package com.rainbowforest.hrmservice.dto;

import com.rainbowforest.hrmservice.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AttendanceDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotNull
        private Long employeeId;
        @NotNull
        private LocalDate attendanceDate;
        private LocalTime checkIn;
        private LocalTime checkOut;
        private Double overtimeHours;
        private AttendanceStatus status;
        private String note;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String employeeCode;
        private LocalDate attendanceDate;
        private LocalTime checkIn;
        private LocalTime checkOut;
        private Double workedHours;
        private Double overtimeHours;
        private AttendanceStatus status;
        private String note;
        private Long confirmedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MonthlySummary {
        private Long employeeId;
        private String employeeName;
        private int month;
        private int year;
        private long presentDays;
        private long absentDays;
        private long lateDays;
        private long halfDays;
        private long leaveDays;
        private double totalWorkedHours;
        private double totalOvertimeHours;
    }
}
