package com.rainbowforest.accountingservice.dto;

import com.rainbowforest.accountingservice.enums.ExpenseCategory;
import com.rainbowforest.accountingservice.enums.ExpenseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotBlank
        private String title;
        private String description;
        @NotNull
        private ExpenseCategory category;
        @NotNull @Positive
        private BigDecimal amount;
        private String currency;
        @NotNull
        private LocalDate expenseDate;
        private String vendor;
        private String receiptUrl;
        private Long departmentId;
        private String departmentName;
        private String requestedBy;
        private String note;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String expenseCode;
        private String title;
        private String description;
        private ExpenseCategory category;
        private ExpenseStatus status;
        private BigDecimal amount;
        private String currency;
        private LocalDate expenseDate;
        private LocalDate paymentDate;
        private String vendor;
        private String receiptUrl;
        private Long departmentId;
        private String departmentName;
        private String requestedBy;
        private String approvedBy;
        private String note;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
