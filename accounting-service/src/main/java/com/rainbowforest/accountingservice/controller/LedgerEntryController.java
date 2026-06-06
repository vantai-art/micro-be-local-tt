package com.rainbowforest.accountingservice.controller;

import com.rainbowforest.accountingservice.dto.LedgerEntryDto;
import com.rainbowforest.accountingservice.service.LedgerEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/accounting/ledger")
@RequiredArgsConstructor
public class LedgerEntryController {

    private final LedgerEntryService ledgerEntryService;

    @PostMapping
    public ResponseEntity<LedgerEntryDto.Response> create(@Valid @RequestBody LedgerEntryDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerEntryService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LedgerEntryDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ledgerEntryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<LedgerEntryDto.Response>> getAll() {
        return ResponseEntity.ok(ledgerEntryService.getAll());
    }

    @GetMapping("/account/{accountCode}")
    public ResponseEntity<List<LedgerEntryDto.Response>> getByAccount(@PathVariable String accountCode) {
        return ResponseEntity.ok(ledgerEntryService.getByAccount(accountCode));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<LedgerEntryDto.Response>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ledgerEntryService.getByDateRange(from, to));
    }

    @GetMapping("/account/{accountCode}/date-range")
    public ResponseEntity<List<LedgerEntryDto.Response>> getByAccountAndDateRange(
            @PathVariable String accountCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ledgerEntryService.getByAccountAndDateRange(accountCode, from, to));
    }

    @GetMapping("/reference")
    public ResponseEntity<List<LedgerEntryDto.Response>> getByReference(
            @RequestParam String referenceType,
            @RequestParam Long referenceId) {
        return ResponseEntity.ok(ledgerEntryService.getByReference(referenceType, referenceId));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<LedgerEntryDto.SummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ledgerEntryService.getSummaryByAccount(from, to));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ledgerEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
