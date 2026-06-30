package com.rainbowforest.hrmservice.service.impl;

import com.rainbowforest.hrmservice.domain.Branch;
import com.rainbowforest.hrmservice.dto.BranchDto;
import com.rainbowforest.hrmservice.repository.BranchRepository;
import com.rainbowforest.hrmservice.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public BranchDto.Response create(BranchDto.Request request) {
        Branch branch = Branch.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .radiusMeters(request.getRadiusMeters())
                .bssid(request.getBssid())
                .ssid(request.getSsid())
                .isDemo(request.getIsDemo() != null ? request.getIsDemo() : true)
                .build();
        return toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public BranchDto.Response update(Long id, BranchDto.Request request) {
        Branch branch = findById(id);
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setLatitude(request.getLatitude());
        branch.setLongitude(request.getLongitude());
        branch.setRadiusMeters(request.getRadiusMeters());
        branch.setBssid(request.getBssid());
        branch.setSsid(request.getSsid());
        if (request.getIsDemo() != null) branch.setIsDemo(request.getIsDemo());
        return toResponse(branchRepository.save(branch));
    }

    @Override
    public BranchDto.Response getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public List<BranchDto.Response> getAll() {
        return branchRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        branchRepository.delete(findById(id));
    }

    private Branch findById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chi nhánh id: " + id));
    }

    private BranchDto.Response toResponse(Branch b) {
        return BranchDto.Response.builder()
                .id(b.getId())
                .name(b.getName())
                .address(b.getAddress())
                .latitude(b.getLatitude())
                .longitude(b.getLongitude())
                .radiusMeters(b.getRadiusMeters())
                .bssid(b.getBssid())
                .ssid(b.getSsid())
                .isDemo(b.getIsDemo())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
