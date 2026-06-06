package com.rainbowforest.hrmservice.domain;

import com.rainbowforest.hrmservice.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payrolls",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "pay_month", "pay_year"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "pay_month", nullable = false)
    private Integer payMonth;

    @Column(name = "pay_year", nullable = false)
    private Integer payYear;

    // Số ngày công chuẩn trong tháng
    @Column(name = "standard_working_days")
    private Integer standardWorkingDays;

    // Số ngày thực tế làm việc
    @Column(name = "actual_working_days")
    private Integer actualWorkingDays;

    // Số ngày nghỉ phép có lương
    @Column(name = "paid_leave_days")
    private Integer paidLeaveDays = 0;

    // Số giờ làm thêm
    @Column(name = "overtime_hours")
    private Double overtimeHours = 0.0;

    // Lương cơ bản
    @Column(name = "basic_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal basicSalary;

    // Phụ cấp
    @Column(name = "allowance", precision = 15, scale = 2)
    private BigDecimal allowance = BigDecimal.ZERO;

    // Lương làm thêm giờ (1.5x)
    @Column(name = "overtime_pay", precision = 15, scale = 2)
    private BigDecimal overtimePay = BigDecimal.ZERO;

    // Thưởng
    @Column(name = "bonus", precision = 15, scale = 2)
    private BigDecimal bonus = BigDecimal.ZERO;

    // Khấu trừ BHXH (8%)
    @Column(name = "social_insurance", precision = 15, scale = 2)
    private BigDecimal socialInsurance = BigDecimal.ZERO;

    // Khấu trừ BHYT (1.5%)
    @Column(name = "health_insurance", precision = 15, scale = 2)
    private BigDecimal healthInsurance = BigDecimal.ZERO;

    // Thuế thu nhập cá nhân
    @Column(name = "personal_income_tax", precision = 15, scale = 2)
    private BigDecimal personalIncomeTax = BigDecimal.ZERO;

    // Các khoản khấu trừ khác
    @Column(name = "other_deductions", precision = 15, scale = 2)
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    // Lương thực nhận = basicSalary + allowance + overtimePay + bonus - socialInsurance - healthInsurance - personalIncomeTax - otherDeductions
    @Column(name = "net_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(name = "note", length = 500)
    private String note;

    // Người duyệt
    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

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
