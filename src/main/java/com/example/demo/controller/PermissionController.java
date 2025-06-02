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
            dto.setUserIds(new java.util.ArrayList<>(ps.getUserIds()));
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping("/sets")
    public PermissionSetDto createSet(@RequestBody CreatePermissionSetRequest req) {
        PermissionSet ps = new PermissionSet();
        ps.setName(req.getName());
        if (req.getUserIds() != null) {
            ps.setUserIds(new java.util.HashSet<>(req.getUserIds()));
        }
        ps = psRepo.save(ps);
        PermissionSetDto dto = new PermissionSetDto();
        dto.setId(ps.getId());
        dto.setName(ps.getName());
        dto.setUserIds(new java.util.ArrayList<>(ps.getUserIds()));
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
    public java.util.List<FieldPermissionDto> addFields(@PathVariable Long id,
                                                         @RequestBody java.util.List<CreateFieldPermissionRequest> reqs) {
        PermissionSet ps = psRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return reqs.stream().map(req -> {
            String objectName = req.getObjectName();
            java.util.List<String> fieldsList = req.getFields();
            String csv = String.join(",", fieldsList);
            // Check existing permission for this set and object
            java.util.Optional<FieldPermission> existing =
                fpRepo.findByPermissionSetAndObjectName(ps, objectName);
            FieldPermission saved;
            if (existing.isPresent()) {
                saved = existing.get();
                // Update if fields changed
                if (!saved.getFields().equals(csv)) {
                    saved.setFields(csv);
                    saved = fpRepo.save(saved);
                }
            } else {
                FieldPermission fp = new FieldPermission();
                fp.setPermissionSet(ps);
                fp.setObjectName(objectName);
                fp.setFields(csv);
                saved = fpRepo.save(fp);
            }
            FieldPermissionDto dto = new FieldPermissionDto();
            dto.setId(saved.getId());
            dto.setPermissionSetId(id);
            dto.setObjectName(saved.getObjectName());
            // Return List<String> version of fields
            dto.setFields(java.util.Arrays.asList(saved.getFields().split(",")));
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    @DeleteMapping("/fields/{fieldId}")
    public void deleteField(@PathVariable Long fieldId) {
        if (!fpRepo.existsById(fieldId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        fpRepo.deleteById(fieldId);
    }

    @PostMapping("/sets/{id}/rows")
    public java.util.List<RowPermissionDto> addRows(@PathVariable Long id,
                                                    @RequestBody java.util.List<CreateRowPermissionRequest> reqs) {
        PermissionSet ps = psRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return reqs.stream().map(req -> {
            String objectName = req.getObjectName();
            String fieldName  = req.getFieldName();
            String operator   = req.getOperator();
            String value      = req.getValue();
            // Deduplicate existing rule
            RowPermission existing = rpRepo.findByPermissionSetInAndObjectName(
                    java.util.List.of(ps), objectName)
                .stream()
                .filter(rp -> fieldName.equals(rp.getFieldName())
                           && operator.equals(rp.getOperator())
                           && value.equals(rp.getValue()))
                .findFirst().orElse(null);
            RowPermission saved;
            if (existing != null) {
                saved = existing;
            } else {
                RowPermission rp = new RowPermission();
                rp.setPermissionSet(ps);
                rp.setObjectName(objectName);
                rp.setFieldName(fieldName);
                rp.setOperator(operator);
                rp.setValue(value);
                saved = rpRepo.save(rp);
            }
            RowPermissionDto dto = new RowPermissionDto();
            dto.setId(saved.getId());
            dto.setPermissionSetId(id);
            dto.setObjectName(saved.getObjectName());
            dto.setFieldName(saved.getFieldName());
            dto.setOperator(saved.getOperator());
            dto.setValue(saved.getValue());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    @DeleteMapping("/rows/{rowId}")
    public void deleteRow(@PathVariable Long rowId) {
        if (!rpRepo.existsById(rowId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        rpRepo.deleteById(rowId);
    }
}