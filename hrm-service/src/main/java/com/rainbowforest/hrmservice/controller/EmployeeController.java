package com.rainbowforest.hrmservice.controller;

import com.rainbowforest.hrmservice.dto.EmployeeDto;
import com.rainbowforest.hrmservice.enums.EmployeeStatus;
import com.rainbowforest.hrmservice.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hrm/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeDto.Response> create(@Valid @RequestBody EmployeeDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto.Response> update(@PathVariable Long id,
            @Valid @RequestBody EmployeeDto.Request request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<EmployeeDto.Response> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(employeeService.getByEmployeeCode(code));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<EmployeeDto.Response> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(employeeService.getByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDto.Response>> getAll() {
        return ResponseEntity.ok(employeeService.getAll());
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeDto.Response>> getByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(employeeService.getByDepartment(departmentId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeDto.Response>> getByStatus(@PathVariable EmployeeStatus status) {
        return ResponseEntity.ok(employeeService.getByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDto.Response>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(employeeService.search(keyword));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EmployeeDto.Response> changeStatus(@PathVariable Long id,
            @RequestParam EmployeeStatus status) {
        return ResponseEntity.ok(employeeService.changeStatus(id, status));
    }

    // Soft delete — chuyển trạng thái "Đã sa thải", giữ hồ sơ
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Hard delete — xóa hẳn khỏi database (dùng cho record test/sai)
    @DeleteMapping("/{id}/force")
    public ResponseEntity<Void> forceDelete(@PathVariable Long id) {
        employeeService.forceDelete(id);
        return ResponseEntity.noContent().build();
    }
}