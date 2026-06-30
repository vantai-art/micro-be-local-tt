package com.rainbowforest.productcatalogservice.controller;

import com.rainbowforest.productcatalogservice.entity.*;
import com.rainbowforest.productcatalogservice.http.header.HeaderGenerator;
import com.rainbowforest.productcatalogservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired private InventoryService inventoryService;
    @Autowired private HeaderGenerator headerGenerator;

    // ══════════════════════════════════════════════════════════
    // SUPPLIER — /suppliers
    // ══════════════════════════════════════════════════════════
    @GetMapping("/suppliers")
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        List<Supplier> list = inventoryService.getAllSuppliers();
        return list.isEmpty()
                ? new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(list, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }

    @GetMapping("/suppliers/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        return inventoryService.getSupplierById(id)
                .map(s -> new ResponseEntity<>(s, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK))
                .orElse(new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND));
    }

    @PostMapping("/suppliers")
    public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier, HttpServletRequest request) {
        if (supplier.getName() == null || supplier.getName().isBlank())
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        Supplier saved = inventoryService.saveSupplier(supplier);
        return new ResponseEntity<>(saved, headerGenerator.getHeadersForSuccessPostMethod(request, saved.getId()), HttpStatus.CREATED);
    }

    @PutMapping("/suppliers/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier data) {
        return inventoryService.getSupplierById(id).map(s -> {
            if (data.getName() != null) s.setName(data.getName());
            if (data.getContactPerson() != null) s.setContactPerson(data.getContactPerson());
            if (data.getPhoneNumber() != null) s.setPhoneNumber(data.getPhoneNumber());
            if (data.getEmail() != null) s.setEmail(data.getEmail());
            if (data.getAddress() != null) s.setAddress(data.getAddress());
            if (data.getNote() != null) s.setNote(data.getNote());
            return new ResponseEntity<>(inventoryService.saveSupplier(s), headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }).orElse(new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/suppliers/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        if (inventoryService.getSupplierById(id).isEmpty())
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        inventoryService.deleteSupplier(id);
        return new ResponseEntity<>(headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }

    // ══════════════════════════════════════════════════════════
    // WAREHOUSE — /warehouses
    // ══════════════════════════════════════════════════════════
    @GetMapping("/warehouses")
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {
        List<Warehouse> list = inventoryService.getAllWarehouses();
        return list.isEmpty()
                ? new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(list, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }

    @GetMapping("/warehouses/{id}")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable Long id) {
        return inventoryService.getWarehouseById(id)
                .map(w -> new ResponseEntity<>(w, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK))
                .orElse(new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND));
    }

    @PostMapping("/warehouses")
    public ResponseEntity<Warehouse> createWarehouse(@RequestBody Warehouse warehouse, HttpServletRequest request) {
        if (warehouse.getName() == null || warehouse.getName().isBlank())
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        Warehouse saved = inventoryService.saveWarehouse(warehouse);
        return new ResponseEntity<>(saved, headerGenerator.getHeadersForSuccessPostMethod(request, saved.getId()), HttpStatus.CREATED);
    }

    @PutMapping("/warehouses/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable Long id, @RequestBody Warehouse data) {
        return inventoryService.getWarehouseById(id).map(w -> {
            if (data.getName() != null) w.setName(data.getName());
            if (data.getLocation() != null) w.setLocation(data.getLocation());
            if (data.getManagerName() != null) w.setManagerName(data.getManagerName());
            if (data.getPhoneNumber() != null) w.setPhoneNumber(data.getPhoneNumber());
            if (data.getNote() != null) w.setNote(data.getNote());
            return new ResponseEntity<>(inventoryService.saveWarehouse(w), headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }).orElse(new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/warehouses/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        if (inventoryService.getWarehouseById(id).isEmpty())
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        inventoryService.deleteWarehouse(id);
        return new ResponseEntity<>(headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }

    // ══════════════════════════════════════════════════════════
    // STOCK — /stock (legacy) + /stocks (FE alias)
    // ══════════════════════════════════════════════════════════
    @GetMapping({"/stock", "/stocks"})
    public ResponseEntity<List<Stock>> getAllStock() {
        List<Stock> list = inventoryService.getAllStock();
        return list.isEmpty()
                ? new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(list, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }

    @GetMapping("/stock/low")
    public ResponseEntity<List<Stock>> getLowStock() {
        return new ResponseEntity<>(inventoryService.getLowStockItems(), headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }

    @GetMapping("/stock/product/{productId}")
    public ResponseEntity<List<Stock>> getStockByProduct(@PathVariable Long productId) {
        List<Stock> list = inventoryService.getStockByProduct(productId);
        return list.isEmpty()
                ? new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(list, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }

    @GetMapping("/stock/warehouse/{warehouseId}")
    public ResponseEntity<List<Stock>> getStockByWarehouse(@PathVariable Long warehouseId) {
        List<Stock> list = inventoryService.getStockByWarehouse(warehouseId);
        return list.isEmpty()
                ? new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(list, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }

    @PostMapping({"/stock", "/stocks"})
    public ResponseEntity<Stock> upsertStock(@RequestBody Map<String, Object> body) {
        try {
            Long productId = Long.parseLong(body.get("productId").toString());
            Long warehouseId = Long.parseLong(body.get("warehouseId").toString());
            int quantity = Integer.parseInt(body.get("quantity").toString());
            int minThreshold = body.containsKey("minThreshold") ? Integer.parseInt(body.get("minThreshold").toString()) : 0;
            String unit = body.containsKey("unit") ? body.get("unit").toString() : null;
            Stock saved = inventoryService.saveOrUpdateStock(productId, warehouseId, quantity, minThreshold, unit);
            return new ResponseEntity<>(saved, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/stock/adjust")
    public ResponseEntity<Stock> adjustStock(@RequestBody Map<String, Object> body) {
        try {
            Long productId = Long.parseLong(body.get("productId").toString());
            Long warehouseId = Long.parseLong(body.get("warehouseId").toString());
            int delta = Integer.parseInt(body.get("delta").toString());
            return new ResponseEntity<>(inventoryService.adjustStock(productId, warehouseId, delta), headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }
    }

    // ══════════════════════════════════════════════════════════
    // GOODS RECEIPT — /goods-receipts + /goods-receipt (FE alias)
    // ══════════════════════════════════════════════════════════
    @GetMapping({"/goods-receipts", "/goods-receipt"})
    public ResponseEntity<List<GoodsReceipt>> getAllReceipts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String status) {
        List<GoodsReceipt> list;
        if (from != null && to != null) list = inventoryService.getReceiptsByDateRange(from, to);
        else if (status != null) list = inventoryService.getReceiptsByStatus(status);
        else list = inventoryService.getAllReceipts();
        return list.isEmpty()
                ? new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(list, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }

    @GetMapping({"/goods-receipts/{id}", "/goods-receipt/{id}"})
    public ResponseEntity<GoodsReceipt> getReceiptById(@PathVariable Long id) {
        return inventoryService.getReceiptById(id)
                .map(r -> new ResponseEntity<>(r, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK))
                .orElse(new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND));
    }

    @PostMapping({"/goods-receipts", "/goods-receipt"})
    public ResponseEntity<GoodsReceipt> createReceipt(@RequestBody GoodsReceipt receipt, HttpServletRequest request) {
        try {
            if (receipt.getItems() != null) receipt.getItems().forEach(item -> item.setGoodsReceipt(receipt));
            GoodsReceipt saved = inventoryService.createReceipt(receipt);
            return new ResponseEntity<>(saved, headerGenerator.getHeadersForSuccessPostMethod(request, saved.getId()), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping({"/goods-receipts/{id}/confirm", "/goods-receipt/{id}/confirm"})
    public ResponseEntity<GoodsReceipt> confirmReceipt(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(inventoryService.confirmReceipt(id), headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping({"/goods-receipts/{id}/cancel", "/goods-receipt/{id}/cancel"})
    public ResponseEntity<GoodsReceipt> cancelReceipt(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(inventoryService.cancelReceipt(id), headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping({"/goods-receipts/{id}", "/goods-receipt/{id}"})
    public ResponseEntity<Void> deleteReceipt(@PathVariable Long id) {
        try {
            inventoryService.deleteReceipt(id);
            return new ResponseEntity<>(headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }
    }

    // ══════════════════════════════════════════════════════════
    // GOODS ISSUE — /goods-issue (FE gọi, BE chưa có → trả về 501 tạm)
    // ══════════════════════════════════════════════════════════
    @PostMapping("/goods-issue")
    public ResponseEntity<Map<String, String>> createGoodsIssue(@RequestBody Map<String, Object> body) {
        // TODO: implement goods issue logic
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Goods issue chưa được implement"));
    }
}
