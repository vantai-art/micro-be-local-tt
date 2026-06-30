package com.rainbowforest.hrmservice.repository;

import com.rainbowforest.hrmservice.domain.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, Long> {
}
