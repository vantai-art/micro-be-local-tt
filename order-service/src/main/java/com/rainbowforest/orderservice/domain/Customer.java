package com.rainbowforest.orderservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Customer — đại diện cho khách hàng đặt hàng.
 * Tách riêng khỏi User (nhân viên hệ thống) để order-service
 * không phụ thuộc vào user-service khi lưu thông tin người mua.
 *
 * Một Customer có thể có nhiều Order (1-N).
 */
@Entity
@Table(name = "customers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên đầy đủ của khách hàng */
    @Column(name = "full_name", nullable = false, length = 100)
    @NotNull
    private String fullName;

    /** Số điện thoại — dùng để liên lạc / tra cứu */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /** Email (tùy chọn) */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * Ghi chú nội bộ (vd: "khách VIP", "dị ứng hải sản").
     * Không bắt buộc.
     */
    @Column(name = "note", length = 255)
    private String note;

    /**
     * ID tham chiếu sang user-service (nếu khách đã có tài khoản).
     * Nullable — khách vãng lai không cần tài khoản.
     */
    @Column(name = "user_id")
    private Long userId;

    public Customer() {}

    public Customer(String fullName, String phoneNumber) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
