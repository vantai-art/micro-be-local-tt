package com.rainbowforest.hrmservice.controller;

import com.rainbowforest.hrmservice.domain.Department;
import com.rainbowforest.hrmservice.repository.DepartmentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/hrm/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @PostMapping
    public ResponseEntity<Department> create(@Valid @RequestBody Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentRepository.save(department));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> update(@PathVariable Long id, @Valid @RequestBody Department body) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phòng ban id: " + id));
        dept.setName(body.getName());
        dept.setCode(body.getCode());
        dept.setDescription(body.getDescription());
        dept.setManagerId(body.getManagerId());
        dept.setActive(body.getActive());
        return ResponseEntity.ok(departmentRepository.save(dept));
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAll() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Department>> getActive() {
        return ResponseEntity.ok(departmentRepository.findByActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phòng ban id: " + id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phòng ban id: " + id));
        dept.setActive(false);
        departmentRepository.save(dept);
        return ResponseEntity.noContent().build();
    }
}
