package com.example.demo.controller;

import com.example.demo.domain.PermissionSet;
import com.example.demo.domain.SftpPermission;
import com.example.demo.dto.CreateSftpPermissionRequest;
import com.example.demo.dto.SftpPermissionDto;
import com.example.demo.repository.PermissionSetRepository;
import com.example.demo.repository.SftpPermissionRepository;
import com.example.demo.repository.SftpServerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for managing SFTP/FTP permissions per PermissionSet.
 */
@RestController
@RequestMapping("/api/permissions/sets/{setId}/sftp")
public class SftpPermissionController {
    private final PermissionSetRepository psRepo;
    private final SftpServerRepository serverRepo;
    private final SftpPermissionRepository permRepo;

    public SftpPermissionController(PermissionSetRepository psRepo,
                                    SftpServerRepository serverRepo,
                                    SftpPermissionRepository permRepo) {
        this.psRepo = psRepo;
        this.serverRepo = serverRepo;
        this.permRepo = permRepo;
    }

    @GetMapping
    public List<SftpPermissionDto> list(@PathVariable Long setId) {
        PermissionSet ps = psRepo.findById(setId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return permRepo.findByPermissionSet(ps).stream().map(this::toDto).collect(Collectors.toList());
    }

    @PostMapping
    public SftpPermissionDto create(@PathVariable Long setId,
                                     @RequestBody CreateSftpPermissionRequest req) {
        PermissionSet ps = psRepo.findById(setId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var server = serverRepo.findById(req.getServerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        SftpPermission perm = new SftpPermission();
        perm.setPermissionSet(ps);
        perm.setServer(server);
        perm.setPathPattern(req.getPathPattern());
        perm.setOperations(String.join(",", req.getOperations()));
        perm = permRepo.save(perm);
        return toDto(perm);
    }

    @DeleteMapping("/{permId}")
    public void delete(@PathVariable Long setId, @PathVariable Long permId) {
        if (!psRepo.existsById(setId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!permRepo.existsById(permId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        permRepo.deleteById(permId);
    }

    private SftpPermissionDto toDto(SftpPermission perm) {
        SftpPermissionDto dto = new SftpPermissionDto();
        dto.setId(perm.getId());
        dto.setPermissionSetId(perm.getPermissionSet().getId());
        dto.setServerId(perm.getServer().getId());
        dto.setPathPattern(perm.getPathPattern());
        dto.setOperations(
            List.of(perm.getOperations().split(",")).stream().map(String::trim).collect(Collectors.toList())
        );
        return dto;
    }
}