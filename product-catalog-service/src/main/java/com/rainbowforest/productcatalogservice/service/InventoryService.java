package com.rainbowforest.productcatalogservice.service;

import com.rainbowforest.productcatalogservice.entity.*;
import com.rainbowforest.productcatalogservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Xử lý nghiệp vụ kho: nhập hàng, cập nhật tồn kho, kiểm tra hàng thấp.
 */
@Service
@Transactional
public class InventoryService {

    @Autowired private SupplierRepository supplierRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private GoodsReceiptRepository goodsReceiptRepository;
    @Autowired private ProductRepository productRepository;

    // ── Supplier ─────────────────────────────────────────────────────────────
    public List<Supplier> getAllSuppliers() { return supplierRepository.findAll(); }
    public Optional<Supplier> getSupplierById(Long id) { return supplierRepository.findById(id); }
    public Supplier saveSupplier(Supplier s) { return supplierRepository.save(s); }
    public void deleteSupplier(Long id) { supplierRepository.deleteById(id); }

    // ── Warehouse ─────────────────────────────────────────────────────────────
    public List<Warehouse> getAllWarehouses() { return warehouseRepository.findAll(); }
    public Optional<Warehouse> getWarehouseById(Long id) { return warehouseRepository.findById(id); }
    public Warehouse saveWarehouse(Warehouse w) { return warehouseRepository.save(w); }
    public void deleteWarehouse(Long id) { warehouseRepository.deleteById(id); }

    // ── Stock ─────────────────────────────────────────────────────────────────
    public List<Stock> getAllStock() { return stockRepository.findAll(); }

    public List<Stock> getStockByProduct(Long productId) {
        return stockRepository.findByProduct_Id(productId);
    }

    public List<Stock> getStockByWarehouse(Long warehouseId) {
        return stockRepository.findByWarehouse_Id(warehouseId);
    }

    public List<Stock> getLowStockItems() {
        return stockRepository.findLowStockItems();
    }

    public Stock saveOrUpdateStock(Long productId, Long warehouseId, int quantity, int minThreshold, String unit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm id: " + productId));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho id: " + warehouseId));

        Stock stock = stockRepository
                .findByProduct_IdAndWarehouse_Id(productId, warehouseId)
                .orElseGet(() -> {
                    Stock s = new Stock();
                    s.setProduct(product);
                    s.setWarehouse(warehouse);
                    return s;
                });
        stock.setQuantity(quantity);
        if (minThreshold >= 0) stock.setMinThreshold(minThreshold);
        if (unit != null) stock.setUnit(unit);
        return stockRepository.save(stock);
    }

    /**
     * Điều chỉnh tồn kho (delta dương = thêm, âm = giảm).
     * Ném RuntimeException nếu số lượng âm sau điều chỉnh.
     */
    public Stock adjustStock(Long productId, Long warehouseId, int delta) {
        Stock stock = stockRepository
                .findByProduct_IdAndWarehouse_Id(productId, warehouseId)
                .orElseThrow(() -> new RuntimeException(
                        "Không có tồn kho cho product=" + productId + " warehouse=" + warehouseId));
        int newQty = stock.getQuantity() + delta;
        if (newQty < 0) {
            throw new RuntimeException(
                    "Tồn kho không đủ: hiện có " + stock.getQuantity() + ", cần trừ " + Math.abs(delta));
        }
        stock.setQuantity(newQty);
        return stockRepository.save(stock);
    }

    // ── GoodsReceipt ─────────────────────────────────────────────────────────

    public List<GoodsReceipt> getAllReceipts() { return goodsReceiptRepository.findAll(); }

    public Optional<GoodsReceipt> getReceiptById(Long id) { return goodsReceiptRepository.findById(id); }

    public List<GoodsReceipt> getReceiptsByDateRange(LocalDate from, LocalDate to) {
        return goodsReceiptRepository.findByReceiptDateBetween(from, to);
    }

    public List<GoodsReceipt> getReceiptsByStatus(String status) {
        return goodsReceiptRepository.findByStatus(status.toUpperCase());
    }

    /**
     * Tạo phiếu nhập mới ở trạng thái DRAFT.
     * Chưa cập nhật tồn kho — chỉ confirm mới cập nhật.
     */
    public GoodsReceipt createReceipt(GoodsReceipt receipt) {
        // Tự sinh receiptCode nếu chưa có
        if (receipt.getReceiptCode() == null || receipt.getReceiptCode().isBlank()) {
            String code = "GR-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    + "-" + System.currentTimeMillis() % 10000;
            receipt.setReceiptCode(code);
        }
        receipt.setStatus("DRAFT");
        // Tính totalAmount từ items
        recalcTotal(receipt);
        return goodsReceiptRepository.save(receipt);
    }

    /**
     * Xác nhận phiếu nhập → cộng số lượng vào tồn kho.
     * Chỉ cho phép confirm khi status = DRAFT.
     */
    public GoodsReceipt confirmReceipt(Long receiptId) {
        GoodsReceipt receipt = goodsReceiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập id: " + receiptId));

        if (!"DRAFT".equals(receipt.getStatus())) {
            throw new RuntimeException(
                    "Phiếu nhập đã ở trạng thái " + receipt.getStatus() + ", không thể xác nhận lại.");
        }

        Warehouse warehouse = receipt.getWarehouse();
        for (GoodsReceiptItem item : receipt.getItems()) {
            Long productId = item.getProduct().getId();
            Long warehouseId = warehouse.getId();

            Stock stock = stockRepository
                    .findByProduct_IdAndWarehouse_Id(productId, warehouseId)
                    .orElseGet(() -> {
                        Stock s = new Stock();
                        s.setProduct(item.getProduct());
                        s.setWarehouse(warehouse);
                        return s;
                    });
            stock.setQuantity(stock.getQuantity() + item.getQuantity());
            stockRepository.save(stock);
        }

        receipt.setStatus("CONFIRMED");
        return goodsReceiptRepository.save(receipt);
    }

    /**
     * Hủy phiếu nhập (chỉ khi DRAFT).
     */
    public GoodsReceipt cancelReceipt(Long receiptId) {
        GoodsReceipt receipt = goodsReceiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập id: " + receiptId));
        if (!"DRAFT".equals(receipt.getStatus())) {
            throw new RuntimeException(
                    "Chỉ có thể hủy phiếu DRAFT, phiếu hiện đang ở trạng thái: " + receipt.getStatus());
        }
        receipt.setStatus("CANCELLED");
        return goodsReceiptRepository.save(receipt);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private void recalcTotal(GoodsReceipt receipt) {
        BigDecimal total = receipt.getItems().stream()
                .map(i -> i.getSubTotal() != null ? i.getSubTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        receipt.setTotalAmount(total);
    }

    public void deleteReceipt(Long id) {
        GoodsReceipt r = goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập id: " + id));
        if ("CONFIRMED".equals(r.getStatus())) {
            throw new RuntimeException("Không thể xóa phiếu đã CONFIRMED.");
        }
        goodsReceiptRepository.deleteById(id);
    }
}
