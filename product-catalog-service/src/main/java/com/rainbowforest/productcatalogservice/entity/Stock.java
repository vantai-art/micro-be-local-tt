package com.rainbowforest.productcatalogservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Tồn kho: số lượng của một sản phẩm trong một kho cụ thể.
 * Constraint unique (product + warehouse) để tránh duplicate.
 */
@Entity
@Table(
    name = "stock",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_stock_product_warehouse",
        columnNames = {"product_id", "warehouse_id"}
    )
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @NotNull
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @NotNull
    private Warehouse warehouse;

    /** Số lượng hiện tại trong kho */
    @Column(name = "quantity", nullable = false)
    private int quantity = 0;

    /** Ngưỡng tối thiểu — khi dưới mức này cần nhập thêm */
    @Column(name = "min_threshold")
    private int minThreshold = 0;

    /** Đơn vị tính (vd: "kg", "lít", "cái", "thùng") */
    @Column(name = "unit", length = 30)
    private String unit;

    public Stock() {}

    // ── Getters & Setters ────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getMinThreshold() { return minThreshold; }
    public void setMinThreshold(int minThreshold) { this.minThreshold = minThreshold; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    /** Tiện ích: kiểm tra xem hàng có đang thấp dưới ngưỡng không */
    @Transient
    public boolean isLowStock() {
        return quantity <= minThreshold;
    }
}
