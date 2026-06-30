package com.rainbowforest.orderservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ordered_date")
    @NotNull
    private LocalDate orderedDate;

    @Column(name = "status")
    @NotNull
    private String status;

    @Column(name = "total")
    private BigDecimal total;

    // FIX: CascadeType.ALL → PERSIST,MERGE để tránh Hibernate lỗi khi load
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "order_items_mapping", joinColumns = @JoinColumn(name = "order_id"), inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items;

    /**
     * FIX: Bỏ @ManyToOne User (@Entity) → thay bằng 2 cột đơn giản
     * Tránh Hibernate JOIN vào bảng "users" không tồn tại trong orders_db → hết 500
     */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    /**
     * Transient: dùng để tương thích với code cũ gọi order.getUser()
     * Không map vào DB
     */
    @Transient
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "table_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private DiningTable diningTable;

    /** Giữ lại cột cũ để backward-compat với dữ liệu đã có */
    @Column(name = "customer_name")
    private String customerName;

    /**
     * Quan hệ tới Customer entity — thay thế dần chuỗi customerName đơn giản.
     * FetchType.LAZY để tránh N+1 khi load danh sách order.
     * Nullable = true → đơn hàng cũ / walk-in không bắt buộc có Customer.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "customer_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Customer customer;

    // ── Online order fields ─────────────────────────────────────
    @Column(name = "order_type", length = 20)
    private String orderType; // DINE_IN, ONLINE, TAKEAWAY

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod; // CASH, VNPAY, BANK_TRANSFER

    public Order() {
    }

    // ─── Getters & Setters ───────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public void setOrderedDate(LocalDate orderedDate) {
        this.orderedDate = orderedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * FIX: getUser() tổng hợp từ userId/userName → tương thích ngược với code cũ
     */
    public User getUser() {
        if (user != null)
            return user;
        if (userId == null)
            return null;
        User u = new User();
        u.setId(userId);
        u.setUserName(userName);
        return u;
    }

    /**
     * FIX: setUser() tách ra lưu userId/userName thay vì @ManyToOne
     */
    public void setUser(User u) {
        this.user = u;
        if (u != null) {
            this.userId = u.getId();
            this.userName = u.getUserName();
        }
    }

    public DiningTable getDiningTable() {
        return diningTable;
    }

    public void setDiningTable(DiningTable diningTable) {
        this.diningTable = diningTable;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        // Sync customerName cho backward-compat
        if (customer != null && this.customerName == null) {
            this.customerName = customer.getFullName();
        }
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}