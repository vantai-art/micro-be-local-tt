package com.rainbowforest.hrmservice.controller;

import com.rainbowforest.hrmservice.dto.BranchDto;
import com.rainbowforest.hrmservice.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/hrm/branches")
    public ResponseEntity<List<BranchDto.Response>> getAll() {
        return ResponseEntity.ok(branchService.getAll());
    }

    @GetMapping("/hrm/branches/{id}")
    public ResponseEntity<BranchDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.getById(id));
    }

    @PostMapping("/hrm/branches")
    public ResponseEntity<BranchDto.Response> create(@Valid @RequestBody BranchDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.create(request));
    }

    @PutMapping("/hrm/branches/{id}")
    public ResponseEntity<BranchDto.Response> update(@PathVariable Long id, @Valid @RequestBody BranchDto.Request request) {
        return ResponseEntity.ok(branchService.update(id, request));
    }

    @DeleteMapping("/hrm/branches/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        branchService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
