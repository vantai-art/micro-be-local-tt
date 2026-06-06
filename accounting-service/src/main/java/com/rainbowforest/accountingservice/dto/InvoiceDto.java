package com.rainbowforest.accountingservice.dto;

import com.rainbowforest.accountingservice.enums.InvoiceStatus;
import com.rainbowforest.accountingservice.enums.InvoiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotNull
        private InvoiceType type;
        @NotBlank
        private String partnerName;
        private String partnerTaxCode;
        private String partnerAddress;
        private String partnerEmail;
        private String partnerPhone;
        private Long orderId;
        @NotNull
        private LocalDate issueDate;
        private LocalDate dueDate;
        private BigDecimal taxRate;
        private BigDecimal discount;
        private String currency;
        private String note;
        private String createdBy;
        private List<ItemRequest> items;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemRequest {
        @NotBlank
        private String description;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String invoiceNumber;
        private InvoiceType type;
        private InvoiceStatus status;
        private String partnerName;
        private String partnerTaxCode;
        private String partnerAddress;
        private String partnerEmail;
        private String partnerPhone;
        private Long orderId;
        private LocalDate issueDate;
        private LocalDate dueDate;
        private LocalDate paidDate;
        private BigDecimal subtotal;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal discount;
        private BigDecimal totalAmount;
        private String currency;
        private String note;
        private String createdBy;
        private String approvedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<ItemResponse> items;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemResponse {
        private Long id;
        private String description;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
    }
}
