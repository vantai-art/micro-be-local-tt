package com.rainbowforest.hrmservice.controller;

import com.rainbowforest.hrmservice.dto.LeaveRequestDto;
import com.rainbowforest.hrmservice.enums.LeaveStatus;
import com.rainbowforest.hrmservice.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hrm/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    public ResponseEntity<LeaveRequestDto.Response> create(@Valid @RequestBody LeaveRequestDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequestDto.Response>> getAll() {
        return ResponseEntity.ok(leaveRequestService.getAll());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequestDto.Response>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveRequestService.getByEmployee(employeeId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LeaveRequestDto.Response>> getByStatus(@PathVariable LeaveStatus status) {
        return ResponseEntity.ok(leaveRequestService.getByStatus(status));
    }

    // Duyệt đơn
    @PatchMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestDto.Response> approve(
            @PathVariable Long id,
            @RequestParam Long approverId) {
        return ResponseEntity.ok(leaveRequestService.approve(id, approverId));
    }

    // Từ chối đơn
    @PatchMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestDto.Response> reject(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestBody LeaveRequestDto.ApproveRequest body) {
        return ResponseEntity.ok(leaveRequestService.reject(id, approverId, body.getRejectReason()));
    }

    // Hủy đơn
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequestDto.Response> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.cancel(id));
    }
}
