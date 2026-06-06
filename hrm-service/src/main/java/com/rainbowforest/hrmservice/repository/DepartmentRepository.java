package com.rainbowforest.hrmservice.repository;

import com.rainbowforest.hrmservice.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCode(String code);
    List<Department> findByActiveTrue();
    boolean existsByName(String name);
    boolean existsByCode(String code);
}
