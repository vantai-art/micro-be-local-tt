package com.rainbowforest.accountingservice.domain;

import com.rainbowforest.accountingservice.enums.LedgerEntryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_number", unique = true, nullable = false, length = 30)
    private String entryNumber;

    @NotNull
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @NotBlank
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    // Tài khoản kế toán (VD: 111, 131, 331...)
    @NotBlank
    @Column(name = "account_code", nullable = false, length = 20)
    private String accountCode;

    @Column(name = "account_name", length = 100)
    private String accountName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private LedgerEntryType entryType;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    private String currency = "VND";

    // Tham chiếu: hóa đơn, chi phí...
    @Column(name = "reference_type", length = 30)
    private String referenceType;  // INVOICE, EXPENSE, PAYROLL, ...

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(name = "created_by", length = 50)
    private String createdBy;

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
