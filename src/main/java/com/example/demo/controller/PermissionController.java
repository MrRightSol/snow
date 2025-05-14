package com.example.demo.controller;

import com.example.demo.domain.FieldPermission;
import com.example.demo.domain.PermissionSet;
import com.example.demo.domain.RowPermission;
import com.example.demo.dto.*;
import com.example.demo.repository.FieldPermissionRepository;
import com.example.demo.repository.PermissionSetRepository;
import com.example.demo.repository.RowPermissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for managing PermissionSets and their field/row permissions.
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    private final PermissionSetRepository psRepo;
    private final FieldPermissionRepository fpRepo;
    private final RowPermissionRepository rpRepo;

    public PermissionController(PermissionSetRepository psRepo,
                                FieldPermissionRepository fpRepo,
                                RowPermissionRepository rpRepo) {
        this.psRepo = psRepo;
        this.fpRepo = fpRepo;
        this.rpRepo = rpRepo;
    }

    @GetMapping("/sets")
    public List<PermissionSetDto> listSets() {
        return psRepo.findAll().stream().map(ps -> {
            PermissionSetDto dto = new PermissionSetDto();
            dto.setId(ps.getId());
            dto.setName(ps.getName());
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping("/sets")
    public PermissionSetDto createSet(@RequestBody CreatePermissionSetRequest req) {
        PermissionSet ps = new PermissionSet();
        ps.setName(req.getName());
        ps = psRepo.save(ps);
        PermissionSetDto dto = new PermissionSetDto();
        dto.setId(ps.getId());
        dto.setName(ps.getName());
        return dto;
    }

    @DeleteMapping("/sets/{id}")
    public void deleteSet(@PathVariable Long id) {
        if (!psRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        // cascade will delete field/row perms if configured
        psRepo.deleteById(id);
    }

    @PostMapping("/sets/{id}/fields")
    public FieldPermissionDto addField(@PathVariable Long id,
                                       @RequestBody CreateFieldPermissionRequest req) {
        PermissionSet ps = psRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        FieldPermission fp = new FieldPermission();
        fp.setPermissionSet(ps);
        fp.setObjectName(req.getObjectName());
        fp.setFields(req.getFields());
        fp = fpRepo.save(fp);
        FieldPermissionDto dto = new FieldPermissionDto();
        dto.setId(fp.getId());
        dto.setPermissionSetId(id);
        dto.setObjectName(fp.getObjectName());
        dto.setFields(fp.getFields());
        return dto;
    }

    @DeleteMapping("/fields/{fieldId}")
    public void deleteField(@PathVariable Long fieldId) {
        if (!fpRepo.existsById(fieldId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        fpRepo.deleteById(fieldId);
    }

    @PostMapping("/sets/{id}/rows")
    public RowPermissionDto addRow(@PathVariable Long id,
                                    @RequestBody CreateRowPermissionRequest req) {
        PermissionSet ps = psRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        RowPermission rp = new RowPermission();
        rp.setPermissionSet(ps);
        rp.setObjectName(req.getObjectName());
        rp.setExpression(req.getExpression());
        rp = rpRepo.save(rp);
        RowPermissionDto dto = new RowPermissionDto();
        dto.setId(rp.getId());
        dto.setPermissionSetId(id);
        dto.setObjectName(rp.getObjectName());
        dto.setExpression(rp.getExpression());
        return dto;
    }

    @DeleteMapping("/rows/{rowId}")
    public void deleteRow(@PathVariable Long rowId) {
        if (!rpRepo.existsById(rowId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        rpRepo.deleteById(rowId);
    }
}