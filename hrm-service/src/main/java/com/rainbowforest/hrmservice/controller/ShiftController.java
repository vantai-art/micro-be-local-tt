package com.rainbowforest.hrmservice.controller;

import com.rainbowforest.hrmservice.dto.ShiftDto;
import com.rainbowforest.hrmservice.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping("/hrm/shifts")
    public ResponseEntity<List<ShiftDto.Response>> getAll() {
        return ResponseEntity.ok(shiftService.getAll());
    }

    @GetMapping("/hrm/shifts/{id}")
    public ResponseEntity<ShiftDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.getById(id));
    }

    @GetMapping("/hrm/shifts/branch/{branchId}")
    public ResponseEntity<List<ShiftDto.Response>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(shiftService.getByBranch(branchId));
    }

    @PostMapping("/hrm/shifts")
    public ResponseEntity<ShiftDto.Response> create(@Valid @RequestBody ShiftDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftService.create(request));
    }

    @PutMapping("/hrm/shifts/{id}")
    public ResponseEntity<ShiftDto.Response> update(@PathVariable Long id, @Valid @RequestBody ShiftDto.Request request) {
        return ResponseEntity.ok(shiftService.update(id, request));
    }

    @DeleteMapping("/hrm/shifts/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shiftService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
