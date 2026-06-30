package com.rainbowforest.orderservice.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Khách đặt bàn trước — không cần order tại chỗ ngay.
 * Status: PENDING → CONFIRMED → SEATED → COMPLETED | CANCELLED
 */
@Entity
@Table(name = "table_reservations")
public class TableReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Bàn được đặt (nullable — admin confirm mới assign) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "table_id")
    private DiningTable table;

    /** userId nếu khách có tài khoản */
    @Column(name = "user_id")
    private Long userId;

    /** Tên khách (nếu đặt không cần đăng nhập) */
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    /** SĐT khách */
    @Column(name = "customer_phone")
    private String customerPhone;

    /** Email khách */
    @Column(name = "customer_email")
    private String customerEmail;

    /** Thời gian đến */
    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    /** Số khách */
    @Column(name = "party_size", nullable = false)
    private Integer partySize = 1;

    /**
     * Trạng thái: PENDING | CONFIRMED | SEATED | COMPLETED | CANCELLED
     */
    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    /** Ghi chú của khách (dị ứng, dịp đặc biệt...) */
    @Column(name = "notes", length = 500)
    private String notes;

    /** Thời điểm tạo */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Constructors ────────────────────────────────────────────────
    public TableReservation() {}

    // ── Getters & Setters ───────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DiningTable getTable() { return table; }
    public void setTable(DiningTable table) { this.table = table; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public Integer getPartySize() { return partySize; }
    public void setPartySize(Integer partySize) { this.partySize = partySize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
