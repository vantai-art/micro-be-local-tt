package com.rainbowforest.accountingservice.domain;

import com.rainbowforest.accountingservice.enums.ExpenseCategory;
import com.rainbowforest.accountingservice.enums.ExpenseStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expense_code", unique = true, nullable = false, length = 20)
    private String expenseCode;

    @NotBlank
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExpenseStatus status = ExpenseStatus.PENDING;

    @Positive
    @NotNull
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    private String currency = "VND";

    @NotNull
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    // Nhà cung cấp / người thụ hưởng
    @Column(name = "vendor", length = 150)
    private String vendor;

    @Column(name = "receipt_url", length = 255)
    private String receiptUrl;

    // Liên kết phòng ban từ hrm-service
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    // Người đề xuất (user_id từ user-service)
    @Column(name = "requested_by", length = 50)
    private String requestedBy;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
