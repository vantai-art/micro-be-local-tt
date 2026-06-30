package com.rainbowforest.hrmservice.dto;

import com.rainbowforest.hrmservice.enums.EmployeeStatus;
import com.rainbowforest.hrmservice.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotBlank
        private String fullName;
        @Email
        private String email;
        private String phone;
        private Gender gender;
        private LocalDate dateOfBirth;
        private String address;
        private String identityCard;
        @NotNull
        private LocalDate hireDate;
        private BigDecimal basicSalary;
        private BigDecimal allowance;
        private Long departmentId;
        private Long positionId;
        private Long branchId;
        private Long userId;
        private Integer annualLeaveDays;
        private String avatarUrl;
        private String note;
        // Dùng cho activity log — không lưu vào entity Employee
        private String reason;
        private String evidence;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String employeeCode;
        private String fullName;
        private String email;
        private String phone;
        private Gender gender;
        private LocalDate dateOfBirth;
        private String address;
        private String identityCard;
        private LocalDate hireDate;
        private LocalDate resignDate;
        private EmployeeStatus status;
        private BigDecimal basicSalary;
        private BigDecimal allowance;
        private Long departmentId;
        private String departmentName;
        private Long positionId;
        private String positionName;
        private Long branchId;
        private String branchName;
        private Long userId;
        private Integer annualLeaveDays;
        private String avatarUrl;
        private String note;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        // Branch WiFi / GPS info for mobile check-in
        private String branchSsid;
        private String branchBssid;
        private Double branchLatitude;
        private Double branchLongitude;
        private Double branchRadiusMeters;
        private Boolean branchIsDemo;
    }
}
