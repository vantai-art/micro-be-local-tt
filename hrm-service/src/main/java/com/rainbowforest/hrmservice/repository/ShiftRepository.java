package com.rainbowforest.hrmservice.repository;

import com.rainbowforest.hrmservice.domain.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByBranchId(Long branchId);
}
