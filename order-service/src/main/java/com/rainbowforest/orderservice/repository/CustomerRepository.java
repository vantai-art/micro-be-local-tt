package com.rainbowforest.orderservice.repository;

import com.rainbowforest.orderservice.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** Tìm khách theo số điện thoại (để tránh tạo trùng) */
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    /** Tìm khách theo tên (có thể trả về nhiều kết quả) */
    List<Customer> findByFullNameContainingIgnoreCase(String fullName);

    /** Tìm theo userId từ user-service */
    Optional<Customer> findByUserId(Long userId);
}
