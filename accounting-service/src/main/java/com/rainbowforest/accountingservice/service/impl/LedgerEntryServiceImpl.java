package com.rainbowforest.accountingservice.service.impl;

import com.rainbowforest.accountingservice.domain.LedgerEntry;
import com.rainbowforest.accountingservice.dto.LedgerEntryDto;
import com.rainbowforest.accountingservice.enums.LedgerEntryType;
import com.rainbowforest.accountingservice.repository.LedgerEntryRepository;
import com.rainbowforest.accountingservice.service.LedgerEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerEntryServiceImpl implements LedgerEntryService {

    private final LedgerEntryRepository ledgerEntryRepository;

    @Override
    @Transactional
    public LedgerEntryDto.Response create(LedgerEntryDto.Request request) {
        String entryNumber = generateEntryNumber();

        LedgerEntry entry = LedgerEntry.builder()
                .entryNumber(entryNumber)
                .entryDate(request.getEntryDate())
                .description(request.getDescription())
                .accountCode(request.getAccountCode())
                .accountName(request.getAccountName())
                .entryType(request.getEntryType())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .referenceNumber(request.getReferenceNumber())
                .createdBy(request.getCreatedBy())
                .note(request.getNote())
                .build();

        entry = ledgerEntryRepository.save(entry);
        log.info("Tạo bút toán sổ cái: {} | TK {} | {} | {}",
                entryNumber, request.getAccountCode(), request.getEntryType(), request.getAmount());
        return toResponse(entry);
    }

    @Override
    public LedgerEntryDto.Response getById(Long id) {
        return toResponse(ledgerEntryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bút toán id: " + id)));
    }

    @Override
    public List<LedgerEntryDto.Response> getAll() {
        return ledgerEntryRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<LedgerEntryDto.Response> getByAccount(String accountCode) {
        return ledgerEntryRepository.findByAccountCode(accountCode)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<LedgerEntryDto.Response> getByDateRange(LocalDate from, LocalDate to) {
        return ledgerEntryRepository.findByEntryDateBetween(from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<LedgerEntryDto.Response> getByAccountAndDateRange(String accountCode, LocalDate from, LocalDate to) {
        return ledgerEntryRepository.findByAccountCodeAndEntryDateBetween(accountCode, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<LedgerEntryDto.Response> getByReference(String referenceType, Long referenceId) {
        return ledgerEntryRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<LedgerEntryDto.SummaryResponse> getSummaryByAccount(LocalDate from, LocalDate to) {
        List<String> accountCodes = ledgerEntryRepository.findDistinctAccountCodes();
        return accountCodes.stream().map(code -> {
            BigDecimal totalDebit = ledgerEntryRepository.sumByAccountAndTypeAndDateRange(
                    code, LedgerEntryType.DEBIT, from, to);
            BigDecimal totalCredit = ledgerEntryRepository.sumByAccountAndTypeAndDateRange(
                    code, LedgerEntryType.CREDIT, from, to);
            BigDecimal balance = totalDebit.subtract(totalCredit);
            return LedgerEntryDto.SummaryResponse.builder()
                    .accountCode(code)
                    .totalDebit(totalDebit)
                    .totalCredit(totalCredit)
                    .balance(balance)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        LedgerEntry entry = ledgerEntryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bút toán id: " + id));
        ledgerEntryRepository.delete(entry);
        log.info("Đã xóa bút toán id: {}", id);
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private String generateEntryNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = ledgerEntryRepository.count() + 1;
        String candidate = String.format("GL-%s-%05d", datePart, count);
        while (ledgerEntryRepository.existsByEntryNumber(candidate)) {
            count++;
            candidate = String.format("GL-%s-%05d", datePart, count);
        }
        return candidate;
    }

    private LedgerEntryDto.Response toResponse(LedgerEntry e) {
        return LedgerEntryDto.Response.builder()
                .id(e.getId())
                .entryNumber(e.getEntryNumber())
                .entryDate(e.getEntryDate())
                .description(e.getDescription())
                .accountCode(e.getAccountCode())
                .accountName(e.getAccountName())
                .entryType(e.getEntryType())
                .amount(e.getAmount())
                .currency(e.getCurrency())
                .referenceType(e.getReferenceType())
                .referenceId(e.getReferenceId())
                .referenceNumber(e.getReferenceNumber())
                .createdBy(e.getCreatedBy())
                .note(e.getNote())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
