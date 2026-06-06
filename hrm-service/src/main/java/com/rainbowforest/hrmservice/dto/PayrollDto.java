package com.rainbowforest.hrmservice.dto;

import com.rainbowforest.hrmservice.enums.PayrollStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayrollDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CalculateRequest {
        private Long employeeId;
        private int month;
        private int year;
        private BigDecimal bonus;
        private BigDecimal otherDeductions;
        private String note;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String employeeCode;
        private String departmentName;
        private String positionName;
        private int payMonth;
        private int payYear;
        private Integer standardWorkingDays;
        private Integer actualWorkingDays;
        private Integer paidLeaveDays;
        private Double overtimeHours;
        private BigDecimal basicSalary;
        private BigDecimal allowance;
        private BigDecimal overtimePay;
        private BigDecimal bonus;
        private BigDecimal socialInsurance;
        private BigDecimal healthInsurance;
        private BigDecimal personalIncomeTax;
        private BigDecimal otherDeductions;
        private BigDecimal netSalary;
        private PayrollStatus status;
        private String note;
        private Long approvedBy;
        private LocalDateTime approvedAt;
        private LocalDateTime paidAt;
        private LocalDateTime createdAt;
    }
}
