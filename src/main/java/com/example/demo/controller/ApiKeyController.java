package com.example.demo.controller;

import com.example.demo.domain.ApiKey;
import com.example.demo.domain.PermissionSet;
import com.example.demo.dto.ApiKeyDto;
import com.example.demo.dto.CreateApiKeyRequest;
import com.example.demo.repository.ApiKeyRepository;
import com.example.demo.repository.PermissionSetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST endpoints for managing API keys.
 */
@RestController
@RequestMapping("/api/apikeys")
public class ApiKeyController {
    private final ApiKeyRepository akRepo;
    private final PermissionSetRepository psRepo;

    public ApiKeyController(ApiKeyRepository akRepo,
                            PermissionSetRepository psRepo) {
        this.akRepo = akRepo;
        this.psRepo = psRepo;
    }

    @GetMapping
    public List<ApiKeyDto> listKeys() {
        return akRepo.findAll().stream().map(ak -> {
            ApiKeyDto dto = new ApiKeyDto();
            dto.setId(ak.getId());
            dto.setKey(ak.getKey());
            dto.setPermissionSetId(ak.getPermissionSet().getId());
            dto.setExpiresAt(ak.getExpiresAt());
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public ApiKeyDto createKey(@RequestBody CreateApiKeyRequest req) {
        PermissionSet ps = psRepo.findById(req.getPermissionSetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PermissionSet not found"));
        ApiKey ak = new ApiKey();
        ak.setKey(UUID.randomUUID().toString());
        ak.setPermissionSet(ps);
        ak.setExpiresAt(req.getExpiresAt() != null ? req.getExpiresAt() : LocalDateTime.now().plusDays(1));
        ak = akRepo.save(ak);
        ApiKeyDto dto = new ApiKeyDto();
        dto.setId(ak.getId());
        dto.setKey(ak.getKey());
        dto.setPermissionSetId(ps.getId());
        dto.setExpiresAt(ak.getExpiresAt());
        return dto;
    }

    @DeleteMapping("/{id}")
    public void deleteKey(@PathVariable Long id) {
        if (!akRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        akRepo.deleteById(id);
    }
}