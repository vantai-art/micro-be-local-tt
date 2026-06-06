package com.rainbowforest.orderservice.controller;

import com.rainbowforest.orderservice.domain.Customer;
import com.rainbowforest.orderservice.http.header.HeaderGenerator;
import com.rainbowforest.orderservice.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * CRUD cho Customer.
 * Base path: /customer
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private HeaderGenerator headerGenerator;

    // ── GET /customer ────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> list = customerRepository.findAll();
        if (!list.isEmpty()) {
            return new ResponseEntity<>(list, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    // ── GET /customer/{id} ───────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(c -> new ResponseEntity<>(c, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK))
                .orElse(new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND));
    }

    // ── GET /customer/search?phone=xxx ───────────────────────────────────────
    @GetMapping(params = "phone")
    public ResponseEntity<Customer> getCustomerByPhone(@RequestParam("phone") String phone) {
        return customerRepository.findByPhoneNumber(phone)
                .map(c -> new ResponseEntity<>(c, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK))
                .orElse(new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND));
    }

    // ── GET /customer/search?name=xxx ────────────────────────────────────────
    @GetMapping(params = "name")
    public ResponseEntity<List<Customer>> searchByName(@RequestParam("name") String name) {
        List<Customer> list = customerRepository.findByFullNameContainingIgnoreCase(name);
        if (!list.isEmpty()) {
            return new ResponseEntity<>(list, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }
        return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
    }

    // ── POST /customer ───────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Customer> createCustomer(
            @RequestBody Customer customer, HttpServletRequest request) {
        if (customer == null || customer.getFullName() == null || customer.getFullName().isBlank()) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.BAD_REQUEST);
        }
        // Tránh tạo trùng theo SĐT
        if (customer.getPhoneNumber() != null && !customer.getPhoneNumber().isBlank()) {
            var existing = customerRepository.findByPhoneNumber(customer.getPhoneNumber());
            if (existing.isPresent()) {
                return new ResponseEntity<>(
                        existing.get(),
                        headerGenerator.getHeadersForSuccessGetMethod(),
                        HttpStatus.OK);
            }
        }
        Customer saved = customerRepository.save(customer);
        return new ResponseEntity<>(
                saved,
                headerGenerator.getHeadersForSuccessPostMethod(request, saved.getId()),
                HttpStatus.CREATED);
    }

    // ── PUT /customer/{id} ───────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id, @RequestBody Customer data) {
        return customerRepository.findById(id).map(c -> {
            if (data.getFullName() != null) c.setFullName(data.getFullName());
            if (data.getPhoneNumber() != null) c.setPhoneNumber(data.getPhoneNumber());
            if (data.getEmail() != null) c.setEmail(data.getEmail());
            if (data.getNote() != null) c.setNote(data.getNote());
            if (data.getUserId() != null) c.setUserId(data.getUserId());
            Customer saved = customerRepository.save(c);
            return new ResponseEntity<>(saved, headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
        }).orElse(new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND));
    }

    // ── DELETE /customer/{id} ────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        if (!customerRepository.existsById(id)) {
            return new ResponseEntity<>(headerGenerator.getHeadersForError(), HttpStatus.NOT_FOUND);
        }
        customerRepository.deleteById(id);
        return new ResponseEntity<>(headerGenerator.getHeadersForSuccessGetMethod(), HttpStatus.OK);
    }
}
