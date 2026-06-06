package com.rainbowforest.accountingservice.domain;

import com.rainbowforest.accountingservice.enums.InvoiceStatus;
import com.rainbowforest.accountingservice.enums.InvoiceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", unique = true, nullable = false, length = 30)
    private String invoiceNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InvoiceType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    // Đối tác (khách hàng hoặc nhà cung cấp)
    @NotBlank
    @Column(name = "partner_name", nullable = false, length = 150)
    private String partnerName;

    @Column(name = "partner_tax_code", length = 20)
    private String partnerTaxCode;

    @Column(name = "partner_address", length = 255)
    private String partnerAddress;

    @Column(name = "partner_email", length = 100)
    private String partnerEmail;

    @Column(name = "partner_phone", length = 20)
    private String partnerPhone;

    // Tham chiếu đơn hàng từ order-service (tùy chọn)
    @Column(name = "order_id")
    private Long orderId;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    // Tổng tiền trước thuế
    @Column(name = "subtotal", precision = 18, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    // Thuế VAT (%)
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate = new BigDecimal("10.00");

    // Tiền thuế
    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    // Giảm giá
    @Column(name = "discount", precision = 18, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    // Tổng tiền phải thanh toán
    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "currency", length = 10)
    private String currency = "VND";

    @Column(name = "note", length = 500)
    private String note;

    // Người tạo
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        recalculate();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        recalculate();
    }

    public void recalculate() {
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (taxRate == null) taxRate = BigDecimal.ZERO;
        if (discount == null) discount = BigDecimal.ZERO;
        this.taxAmount = subtotal.multiply(taxRate).divide(new BigDecimal("100"));
        this.totalAmount = subtotal.add(taxAmount).subtract(discount);
    }
}
