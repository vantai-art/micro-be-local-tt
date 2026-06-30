package com.rainbowforest.orderservice.controller;

import com.rainbowforest.orderservice.domain.Bill;
import com.rainbowforest.orderservice.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bills")
public class BillController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private SimpMessagingTemplate ws;

    @GetMapping
    public ResponseEntity<List<Bill>> getAllBills() {
        return ResponseEntity.ok(billRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        return billRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Bill>> getBillsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(billRepository.findByOrderId(orderId));
    }

    @PostMapping
    public ResponseEntity<Bill> createBill(@RequestBody Bill bill) {
        bill.setCreatedAt(new java.util.Date());
        if (bill.getPaymentStatus() == null) bill.setPaymentStatus("PENDING");
        Bill saved = billRepository.save(bill);
        // Broadcast realtime
        ws.convertAndSend("/topic/bills",
            Map.of("type", "bill:created", "data", saved));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Bill> updateStatus(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        return billRepository.findById(id).map(bill -> {
            String status = body.get("paymentStatus");
            if (status != null) bill.setPaymentStatus(status);
            String txId = body.get("transactionId");
            if (txId != null) bill.setTransactionId(txId);
            Bill saved = billRepository.save(bill);
            ws.convertAndSend("/topic/bills",
                Map.of("type", "bill:updated", "data", saved));
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id) {
        if (!billRepository.existsById(id)) return ResponseEntity.notFound().build();
        billRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
