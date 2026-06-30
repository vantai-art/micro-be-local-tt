package com.rainbowforest.hrmservice.service;

import com.rainbowforest.hrmservice.dto.BranchDto;

import java.util.List;

public interface BranchService {
    BranchDto.Response create(BranchDto.Request request);
    BranchDto.Response update(Long id, BranchDto.Request request);
    BranchDto.Response getById(Long id);
    List<BranchDto.Response> getAll();
    void delete(Long id);
}
