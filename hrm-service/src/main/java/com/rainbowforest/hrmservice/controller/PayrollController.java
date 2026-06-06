package com.rainbowforest.hrmservice.controller;

import com.rainbowforest.hrmservice.dto.PayrollDto;
import com.rainbowforest.hrmservice.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hrm/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/calculate")
    public ResponseEntity<PayrollDto.Response> calculate(@RequestBody PayrollDto.CalculateRequest request) {
        return ResponseEntity.ok(payrollService.calculate(request));
    }

    @PostMapping("/calculate-all")
    public ResponseEntity<PayrollDto.Response> calculateAll(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(payrollService.calculateAll(month, year));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PayrollDto.Response>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollService.getByEmployee(employeeId));
    }

    @GetMapping
    public ResponseEntity<List<PayrollDto.Response>> getByMonthAndYear(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getByMonthAndYear(month, year));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<PayrollDto.Response> approve(
            @PathVariable Long id, @RequestParam Long approverId) {
        return ResponseEntity.ok(payrollService.approve(id, approverId));
    }

    // FE gọi PUT /hrm/payrolls/$id/pay
    @PutMapping("/{id}/pay")
    public ResponseEntity<PayrollDto.Response> pay(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.markPaid(id));
    }

    // Legacy
    @PatchMapping("/{id}/mark-paid")
    public ResponseEntity<PayrollDto.Response> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.markPaid(id));
    }

    @GetMapping("/total")
    public ResponseEntity<?> getTotal(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getTotalPayrollByMonthAndYear(month, year));
    }
}
