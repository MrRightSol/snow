package com.example.demo.controller;

import com.example.demo.domain.DbCredential;
import com.example.demo.domain.DataSourceConfig;
import com.example.demo.domain.PermissionSet;
import com.example.demo.dto.CreateDbCredentialRequest;
import com.example.demo.dto.DbCredentialDto;
import com.example.demo.repository.DbCredentialRepository;
import com.example.demo.repository.DataSourceConfigRepository;
import com.example.demo.repository.PermissionSetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for managing database credentials per DataSource.
 */
@RestController
@RequestMapping("/api/datasources/{dsId}/credentials")
public class DbCredentialController {
    private final DbCredentialRepository credRepo;
    private final DataSourceConfigRepository dsRepo;
    private final PermissionSetRepository psRepo;

    public DbCredentialController(DbCredentialRepository credRepo,
                                  DataSourceConfigRepository dsRepo,
                                  PermissionSetRepository psRepo) {
        this.credRepo = credRepo;
        this.dsRepo = dsRepo;
        this.psRepo = psRepo;
    }

    @GetMapping
    public List<DbCredentialDto> list(@PathVariable Long dsId) {
        if (!dsRepo.existsById(dsId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return credRepo.findByDataSource_Id(dsId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @PostMapping
    public DbCredentialDto create(@PathVariable Long dsId,
                                   @RequestBody CreateDbCredentialRequest req) {
        DataSourceConfig ds = dsRepo.findById(dsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        PermissionSet ps = psRepo.findById(req.getPermissionSetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        DbCredential cred = new DbCredential();
        cred.setDataSource(ds);
        cred.setPermissionSet(ps);
        cred.setUsername(java.util.UUID.randomUUID().toString().replace("-", ""));
        cred.setPassword(java.util.UUID.randomUUID().toString().replace("-", ""));
        cred.setExpiresAt(req.getExpiresAt() != null ? req.getExpiresAt() : java.time.LocalDateTime.now().plusDays(1));
        cred = credRepo.save(cred);
        return toDto(cred);
    }

    @DeleteMapping("/{credId}")
    public void delete(@PathVariable Long dsId, @PathVariable Long credId) {
        if (!dsRepo.existsById(dsId) || !credRepo.existsById(credId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        credRepo.deleteById(credId);
    }

    private DbCredentialDto toDto(DbCredential c) {
        DbCredentialDto dto = new DbCredentialDto();
        dto.setId(c.getId());
        dto.setDataSourceId(c.getDataSource().getId());
        dto.setPermissionSetId(c.getPermissionSet().getId());
        dto.setUsername(c.getUsername());
        dto.setExpiresAt(c.getExpiresAt());
        return dto;
    }
}