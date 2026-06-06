package com.rainbowforest.accountingservice.repository;

import com.rainbowforest.accountingservice.domain.LedgerEntry;
import com.rainbowforest.accountingservice.enums.LedgerEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByAccountCode(String accountCode);

    List<LedgerEntry> findByEntryDateBetween(LocalDate from, LocalDate to);

    List<LedgerEntry> findByAccountCodeAndEntryDateBetween(String accountCode, LocalDate from, LocalDate to);

    List<LedgerEntry> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM LedgerEntry e WHERE e.accountCode = :code AND e.entryType = :type AND e.entryDate BETWEEN :from AND :to")
    BigDecimal sumByAccountAndTypeAndDateRange(@Param("code") String accountCode,
                                               @Param("type") LedgerEntryType type,
                                               @Param("from") LocalDate from,
                                               @Param("to") LocalDate to);

    @Query("SELECT DISTINCT e.accountCode FROM LedgerEntry e ORDER BY e.accountCode")
    List<String> findDistinctAccountCodes();

    boolean existsByEntryNumber(String entryNumber);
}
