package com.rainbowforest.accountingservice.controller;

import com.rainbowforest.accountingservice.dto.InvoiceDto;
import com.rainbowforest.accountingservice.enums.InvoiceStatus;
import com.rainbowforest.accountingservice.enums.InvoiceType;
import com.rainbowforest.accountingservice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/accounting/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceDto.Response> create(@Valid @RequestBody InvoiceDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDto.Response> update(@PathVariable Long id,
                                                       @Valid @RequestBody InvoiceDto.Request request) {
        return ResponseEntity.ok(invoiceService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<InvoiceDto.Response> getByNumber(@PathVariable String number) {
        return ResponseEntity.ok(invoiceService.getByNumber(number));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceDto.Response>> getAll() {
        return ResponseEntity.ok(invoiceService.getAll());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<InvoiceDto.Response>> getByType(@PathVariable InvoiceType type) {
        return ResponseEntity.ok(invoiceService.getByType(type));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceDto.Response>> getByStatus(@PathVariable InvoiceStatus status) {
        return ResponseEntity.ok(invoiceService.getByStatus(status));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<InvoiceDto.Response>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(invoiceService.getByDateRange(from, to));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<InvoiceDto.Response>> getOverdue() {
        return ResponseEntity.ok(invoiceService.getOverdue());
    }

    @GetMapping("/search")
    public ResponseEntity<List<InvoiceDto.Response>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(invoiceService.searchByPartner(keyword));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InvoiceDto.Response> changeStatus(@PathVariable Long id,
                                                             @RequestParam InvoiceStatus status) {
        return ResponseEntity.ok(invoiceService.changeStatus(id, status));
    }

    @PatchMapping("/{id}/paid")
    public ResponseEntity<InvoiceDto.Response> markPaid(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paidDate) {
        return ResponseEntity.ok(invoiceService.markPaid(id, paidDate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(invoiceService.getTotalRevenue(from, to));
    }

    @GetMapping("/summary/expense")
    public ResponseEntity<BigDecimal> getTotalExpenseInvoice(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(invoiceService.getTotalExpenseInvoice(from, to));
    }
}
