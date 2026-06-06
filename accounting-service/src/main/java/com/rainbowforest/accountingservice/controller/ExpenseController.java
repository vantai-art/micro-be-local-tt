package com.rainbowforest.accountingservice.controller;

import com.rainbowforest.accountingservice.dto.ExpenseDto;
import com.rainbowforest.accountingservice.enums.ExpenseCategory;
import com.rainbowforest.accountingservice.enums.ExpenseStatus;
import com.rainbowforest.accountingservice.service.ExpenseService;
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
@RequestMapping("/accounting/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDto.Response> create(@Valid @RequestBody ExpenseDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto.Response> update(@PathVariable Long id,
                                                       @Valid @RequestBody ExpenseDto.Request request) {
        return ResponseEntity.ok(expenseService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto.Response>> getAll() {
        return ResponseEntity.ok(expenseService.getAll());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ExpenseDto.Response>> getByStatus(@PathVariable ExpenseStatus status) {
        return ResponseEntity.ok(expenseService.getByStatus(status));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ExpenseDto.Response>> getByCategory(@PathVariable ExpenseCategory category) {
        return ResponseEntity.ok(expenseService.getByCategory(category));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<ExpenseDto.Response>> getByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(expenseService.getByDepartment(departmentId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ExpenseDto.Response>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(expenseService.getByDateRange(from, to));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ExpenseDto.Response> approve(@PathVariable Long id,
                                                        @RequestParam String approvedBy) {
        return ResponseEntity.ok(expenseService.approve(id, approvedBy));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ExpenseDto.Response> reject(@PathVariable Long id,
                                                       @RequestParam String rejectedBy,
                                                       @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(expenseService.reject(id, rejectedBy, reason));
    }

    @PatchMapping("/{id}/paid")
    public ResponseEntity<ExpenseDto.Response> markPaid(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate) {
        return ResponseEntity.ok(expenseService.markPaid(id, paymentDate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary/total")
    public ResponseEntity<BigDecimal> getTotalPaid(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(expenseService.getTotalPaid(from, to));
    }

    @GetMapping("/summary/category/{category}")
    public ResponseEntity<BigDecimal> getTotalByCategory(
            @PathVariable ExpenseCategory category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(expenseService.getTotalByCategory(category, from, to));
    }

    @GetMapping("/summary/department/{departmentId}")
    public ResponseEntity<BigDecimal> getTotalByDepartment(
            @PathVariable Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(expenseService.getTotalByDepartment(departmentId, from, to));
    }
}
