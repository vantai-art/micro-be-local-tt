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
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    // ── FE endpoints: /hrm/leaves ────────────────────────────────────────────
    @GetMapping("/hrm/leaves")
    public ResponseEntity<List<LeaveRequestDto.Response>> getAllLeaves() {
        return ResponseEntity.ok(leaveRequestService.getAll());
    }

    @PostMapping("/hrm/leaves")
    public ResponseEntity<LeaveRequestDto.Response> createLeave(@Valid @RequestBody LeaveRequestDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.create(request));
    }

    @PutMapping("/hrm/leaves/{id}/approve")
    public ResponseEntity<LeaveRequestDto.Response> approveLeave(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body) {
        Long approverId = Long.parseLong(body.getOrDefault("approverId", "1").toString());
        return ResponseEntity.ok(leaveRequestService.approve(id, approverId));
    }

    // ── Legacy endpoints: /hrm/leave-requests ───────────────────────────────
    @PostMapping("/hrm/leave-requests")
    public ResponseEntity<LeaveRequestDto.Response> create(@Valid @RequestBody LeaveRequestDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.create(request));
    }

    @GetMapping("/hrm/leave-requests/{id}")
    public ResponseEntity<LeaveRequestDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.getById(id));
    }

    @GetMapping("/hrm/leave-requests")
    public ResponseEntity<List<LeaveRequestDto.Response>> getAll() {
        return ResponseEntity.ok(leaveRequestService.getAll());
    }

    @GetMapping("/hrm/leave-requests/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequestDto.Response>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveRequestService.getByEmployee(employeeId));
    }

    @GetMapping("/hrm/leave-requests/status/{status}")
    public ResponseEntity<List<LeaveRequestDto.Response>> getByStatus(@PathVariable LeaveStatus status) {
        return ResponseEntity.ok(leaveRequestService.getByStatus(status));
    }

    @PatchMapping("/hrm/leave-requests/{id}/approve")
    public ResponseEntity<LeaveRequestDto.Response> approve(
            @PathVariable Long id, @RequestParam Long approverId) {
        return ResponseEntity.ok(leaveRequestService.approve(id, approverId));
    }

    @PatchMapping("/hrm/leave-requests/{id}/reject")
    public ResponseEntity<LeaveRequestDto.Response> reject(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestBody LeaveRequestDto.ApproveRequest body) {
        return ResponseEntity.ok(leaveRequestService.reject(id, approverId, body.getRejectReason()));
    }

    @PatchMapping("/hrm/leave-requests/{id}/cancel")
    public ResponseEntity<LeaveRequestDto.Response> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.cancel(id));
    }
}
