package com.rainbowforest.productcatalogservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Dòng chi tiết trong phiếu nhập hàng.
 * Mỗi dòng = 1 sản phẩm với số lượng và đơn giá nhập.
 */
@Entity
@Table(name = "goods_receipt_items")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GoodsReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    @JsonIgnore
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @NotNull
    private Product product;

    /** Số lượng nhập */
    @Column(name = "quantity", nullable = false)
    @Min(1)
    private int quantity;

    /** Đơn giá nhập (giá mua vào từ NCC) */
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal unitPrice;

    /** Thành tiền = quantity × unitPrice (tính sẵn để tránh tính lại) */
    @Column(name = "sub_total", precision = 15, scale = 2)
    private BigDecimal subTotal;

    public GoodsReceiptItem() {}

    /** Tự tính subTotal khi set quantity/unitPrice */
    public void recalcSubTotal() {
        if (unitPrice != null) {
            this.subTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GoodsReceipt getGoodsReceipt() { return goodsReceipt; }
    public void setGoodsReceipt(GoodsReceipt goodsReceipt) { this.goodsReceipt = goodsReceipt; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        recalcSubTotal();
    }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        recalcSubTotal();
    }

    public BigDecimal getSubTotal() { return subTotal; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
}
