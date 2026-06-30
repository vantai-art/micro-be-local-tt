package com.rainbowforest.hrmservice.service.impl;

import com.rainbowforest.hrmservice.domain.Branch;
import com.rainbowforest.hrmservice.domain.Shift;
import com.rainbowforest.hrmservice.dto.ShiftDto;
import com.rainbowforest.hrmservice.repository.BranchRepository;
import com.rainbowforest.hrmservice.repository.ShiftRepository;
import com.rainbowforest.hrmservice.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public ShiftDto.Response create(ShiftDto.Request request) {
        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chi nhánh id: " + request.getBranchId()));
        }
        Shift shift = Shift.builder()
                .name(request.getName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .branch(branch)
                .build();
        return toResponse(shiftRepository.save(shift));
    }

    @Override
    @Transactional
    public ShiftDto.Response update(Long id, ShiftDto.Request request) {
        Shift shift = findById(id);
        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chi nhánh id: " + request.getBranchId()));
            shift.setBranch(branch);
        }
        return toResponse(shiftRepository.save(shift));
    }

    @Override
    public ShiftDto.Response getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public List<ShiftDto.Response> getAll() {
        return shiftRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ShiftDto.Response> getByBranch(Long branchId) {
        return shiftRepository.findByBranchId(branchId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        shiftRepository.delete(findById(id));
    }

    private Shift findById(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy ca làm việc id: " + id));
    }

    private ShiftDto.Response toResponse(Shift s) {
        return ShiftDto.Response.builder()
                .id(s.getId())
                .name(s.getName())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .branchId(s.getBranch() != null ? s.getBranch().getId() : null)
                .branchName(s.getBranch() != null ? s.getBranch().getName() : null)
                .build();
    }
}
