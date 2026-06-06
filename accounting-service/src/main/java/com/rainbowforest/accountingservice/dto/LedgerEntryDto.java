package com.rainbowforest.accountingservice.dto;

import com.rainbowforest.accountingservice.enums.LedgerEntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LedgerEntryDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotNull
        private LocalDate entryDate;
        @NotBlank
        private String description;
        @NotBlank
        private String accountCode;
        private String accountName;
        @NotNull
        private LedgerEntryType entryType;
        @NotNull
        private BigDecimal amount;
        private String currency;
        private String referenceType;
        private Long referenceId;
        private String referenceNumber;
        private String createdBy;
        private String note;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String entryNumber;
        private LocalDate entryDate;
        private String description;
        private String accountCode;
        private String accountName;
        private LedgerEntryType entryType;
        private BigDecimal amount;
        private String currency;
        private String referenceType;
        private Long referenceId;
        private String referenceNumber;
        private String createdBy;
        private String note;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SummaryResponse {
        private String accountCode;
        private String accountName;
        private BigDecimal totalDebit;
        private BigDecimal totalCredit;
        private BigDecimal balance;
    }
}
