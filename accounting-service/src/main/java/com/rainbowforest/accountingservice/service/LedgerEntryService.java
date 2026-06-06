package com.rainbowforest.accountingservice.service;

import com.rainbowforest.accountingservice.dto.LedgerEntryDto;

import java.time.LocalDate;
import java.util.List;

public interface LedgerEntryService {
    LedgerEntryDto.Response create(LedgerEntryDto.Request request);
    LedgerEntryDto.Response getById(Long id);
    List<LedgerEntryDto.Response> getAll();
    List<LedgerEntryDto.Response> getByAccount(String accountCode);
    List<LedgerEntryDto.Response> getByDateRange(LocalDate from, LocalDate to);
    List<LedgerEntryDto.Response> getByAccountAndDateRange(String accountCode, LocalDate from, LocalDate to);
    List<LedgerEntryDto.Response> getByReference(String referenceType, Long referenceId);
    List<LedgerEntryDto.SummaryResponse> getSummaryByAccount(LocalDate from, LocalDate to);
    void delete(Long id);
}
