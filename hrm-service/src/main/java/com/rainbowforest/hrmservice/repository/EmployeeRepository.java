package com.rainbowforest.hrmservice.repository;

import com.rainbowforest.hrmservice.domain.Employee;
import com.rainbowforest.hrmservice.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUserId(Long userId);

    List<Employee> findByDepartmentId(Long departmentId);
    List<Employee> findByStatus(EmployeeStatus status);
    List<Employee> findByDepartmentIdAndStatus(Long departmentId, EmployeeStatus status);

    boolean existsByEmail(String email);
    boolean existsByEmployeeCode(String employeeCode);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:keyword IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Employee> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :deptId AND e.status = 'ACTIVE'")
    long countActiveByDepartment(@Param("deptId") Long deptId);
}
