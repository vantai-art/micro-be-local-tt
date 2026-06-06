package com.rainbowforest.productcatalogservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Phiếu nhập hàng (Goods Receipt).
 * Mỗi phiếu ghi lại: ai nhập, từ NCC nào, vào kho nào, ngày nào,
 * và danh sách các mặt hàng được nhập (GoodsReceiptItem).
 */
@Entity
@Table(name = "goods_receipts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GoodsReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Số phiếu nhập — do người dùng nhập hoặc tự sinh */
    @Column(name = "receipt_code", length = 50, unique = true)
    private String receiptCode;

    @Column(name = "receipt_date", nullable = false)
    @NotNull
    private LocalDate receiptDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @NotNull
    private Warehouse warehouse;

    /** Tổng giá trị phiếu nhập */
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /** Trạng thái: DRAFT | CONFIRMED | CANCELLED */
    @Column(name = "status", length = 20)
    private String status = "DRAFT";

    /** Người tạo phiếu (user_name từ user-service) */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "note", length = 500)
    private String note;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptItem> items = new ArrayList<>();

    public GoodsReceipt() {}

    // ── Getters & Setters ────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReceiptCode() { return receiptCode; }
    public void setReceiptCode(String receiptCode) { this.receiptCode = receiptCode; }

    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<GoodsReceiptItem> getItems() { return items; }
    public void setItems(List<GoodsReceiptItem> items) { this.items = items; }

    /** Tiện ích: thêm item và giữ quan hệ 2 chiều */
    public void addItem(GoodsReceiptItem item) {
        item.setGoodsReceipt(this);
        items.add(item);
    }
}
