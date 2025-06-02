package com.example.demo.controller;

import com.example.demo.domain.OperationPermission;
import com.example.demo.domain.PermissionSet;
import com.example.demo.dto.CreateOperationPermissionRequest;
import com.example.demo.dto.OperationPermissionDto;
import com.example.demo.repository.OperationPermissionRepository;
import com.example.demo.repository.PermissionSetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for managing operation-level permissions per PermissionSet.
 */
@RestController
@RequestMapping("/api/permissions/sets/{setId}/operations")
public class OperationPermissionController {
    private final OperationPermissionRepository opRepo;
    private final PermissionSetRepository psRepo;

    public OperationPermissionController(OperationPermissionRepository opRepo,
                                         PermissionSetRepository psRepo) {
        this.opRepo = opRepo;
        this.psRepo = psRepo;
    }

    @GetMapping
    public List<OperationPermissionDto> list(@PathVariable Long setId) {
        PermissionSet ps = psRepo.findById(setId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return opRepo.findByPermissionSet(ps)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @PostMapping
    public List<OperationPermissionDto> create(@PathVariable Long setId,
                                               @RequestBody List<CreateOperationPermissionRequest> reqs) {
        PermissionSet ps = psRepo.findById(setId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return reqs.stream().map(req -> {
            String obj = req.getObjectName();
            String csv = String.join(",", req.getOperations());
            OperationPermission existing = opRepo
                .findByPermissionSetAndObjectName(ps, obj).orElse(null);
            OperationPermission saved;
            if (existing != null) {
                existing.setOperations(csv);
                saved = opRepo.save(existing);
            } else {
                OperationPermission op = new OperationPermission();
                op.setPermissionSet(ps);
                op.setObjectName(obj);
                op.setOperations(csv);
                saved = opRepo.save(op);
            }
            return toDto(saved);
        }).collect(Collectors.toList());
    }

    @DeleteMapping("/{permId}")
    public void delete(@PathVariable Long setId, @PathVariable Long permId) {
        if (!psRepo.existsById(setId) || !opRepo.existsById(permId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        opRepo.deleteById(permId);
    }

    private OperationPermissionDto toDto(OperationPermission op) {
        OperationPermissionDto dto = new OperationPermissionDto();
        dto.setId(op.getId());
        dto.setPermissionSetId(op.getPermissionSet().getId());
        dto.setObjectName(op.getObjectName());
        dto.setOperations(List.of(op.getOperations().split(",")));
        return dto;
    }
}