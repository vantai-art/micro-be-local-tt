package com.rainbowforest.hrmservice.service;

import com.rainbowforest.hrmservice.dto.ShiftDto;

import java.util.List;

public interface ShiftService {
    ShiftDto.Response create(ShiftDto.Request request);
    ShiftDto.Response update(Long id, ShiftDto.Request request);
    ShiftDto.Response getById(Long id);
    List<ShiftDto.Response> getAll();
    List<ShiftDto.Response> getByBranch(Long branchId);
    void delete(Long id);
}
