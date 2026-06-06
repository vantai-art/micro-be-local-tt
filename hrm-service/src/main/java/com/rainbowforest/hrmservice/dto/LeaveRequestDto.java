package com.rainbowforest.hrmservice.dto;

import com.rainbowforest.hrmservice.enums.LeaveStatus;
import com.rainbowforest.hrmservice.enums.LeaveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveRequestDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotNull
        private Long employeeId;
        @NotNull
        private LeaveType leaveType;
        @NotNull
        private LocalDate startDate;
        @NotNull
        private LocalDate endDate;
        @NotBlank
        private String reason;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApproveRequest {
        private String rejectReason;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String employeeCode;
        private String departmentName;
        private LeaveType leaveType;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalDays;
        private String reason;
        private LeaveStatus status;
        private Long approvedBy;
        private LocalDateTime approvedAt;
        private String rejectReason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
