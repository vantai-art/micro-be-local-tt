package com.rainbowforest.hrmservice.service;

import com.rainbowforest.hrmservice.dto.LeaveRequestDto;
import com.rainbowforest.hrmservice.enums.LeaveStatus;

import java.util.List;

public interface LeaveRequestService {
    LeaveRequestDto.Response create(LeaveRequestDto.Request request);
    LeaveRequestDto.Response getById(Long id);
    List<LeaveRequestDto.Response> getByEmployee(Long employeeId);
    List<LeaveRequestDto.Response> getByStatus(LeaveStatus status);
    LeaveRequestDto.Response approve(Long id, Long approverId);
    LeaveRequestDto.Response reject(Long id, Long approverId, String reason);
    LeaveRequestDto.Response cancel(Long id);
    List<LeaveRequestDto.Response> getAll();
}
