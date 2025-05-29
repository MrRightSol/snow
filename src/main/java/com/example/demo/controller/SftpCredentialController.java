package com.example.demo.controller;

import com.example.demo.domain.SftpCredential;
import com.example.demo.domain.SftpServerConfig;
import com.example.demo.domain.PermissionSet;
import com.example.demo.dto.CreateSftpCredentialRequest;
import com.example.demo.dto.SftpCredentialDto;
import com.example.demo.repository.SftpCredentialRepository;
import com.example.demo.repository.SftpServerRepository;
import com.example.demo.repository.PermissionSetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST endpoints for managing SFTP/FTP credentials.
 */
@RestController
@RequestMapping("/api/sftp-servers/{serverId}/credentials")
public class SftpCredentialController {
    private final SftpCredentialRepository credRepo;
    private final SftpServerRepository serverRepo;
    private final PermissionSetRepository psRepo;

    public SftpCredentialController(SftpCredentialRepository credRepo,
                                    SftpServerRepository serverRepo,
                                    PermissionSetRepository psRepo) {
        this.credRepo = credRepo;
        this.serverRepo = serverRepo;
        this.psRepo = psRepo;
    }

    @GetMapping
    public List<SftpCredentialDto> list(@PathVariable Long serverId) {
        ensureServerExists(serverId);
        return credRepo.findByServer_Id(serverId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public SftpCredentialDto create(@PathVariable Long serverId,
                                     @RequestBody CreateSftpCredentialRequest req) {
        SftpServerConfig server = serverRepo.findById(serverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        PermissionSet ps = psRepo.findById(req.getPermissionSetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        SftpCredential cred = new SftpCredential();
        cred.setServer(server);
        cred.setPermissionSet(ps);
        // generate random username and password
        cred.setUsername(UUID.randomUUID().toString().replace("-", ""));
        cred.setPassword(UUID.randomUUID().toString().replace("-", ""));
        cred.setExpiresAt(Optional.ofNullable(req.getExpiresAt()).orElse(LocalDateTime.now().plusDays(1)));
        cred = credRepo.save(cred);
        return toDto(cred);
    }

    @DeleteMapping("/{credId}")
    public void delete(@PathVariable Long serverId, @PathVariable Long credId) {
        ensureServerExists(serverId);
        SftpCredential cred = credRepo.findById(credId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        credRepo.delete(cred);
    }

    private void ensureServerExists(Long serverId) {
        if (!serverRepo.existsById(serverId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private SftpCredentialDto toDto(SftpCredential cred) {
        SftpCredentialDto dto = new SftpCredentialDto();
        dto.setId(cred.getId());
        dto.setServerId(cred.getServer().getId());
        dto.setPermissionSetId(cred.getPermissionSet().getId());
        dto.setUsername(cred.getUsername());
        dto.setExpiresAt(cred.getExpiresAt());
        return dto;
    }
}