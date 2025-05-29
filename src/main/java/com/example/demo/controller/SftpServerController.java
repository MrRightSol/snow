package com.example.demo.controller;

import com.example.demo.domain.SftpServerConfig;
import com.example.demo.dto.CreateSftpServerRequest;
import com.example.demo.dto.SftpServerConfigDto;
import com.example.demo.repository.SftpServerRepository;
import com.example.demo.service.SftpService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST endpoints for managing FTP/SFTP server configurations.
 */
@RestController
@RequestMapping("/api/sftp-servers")
public class SftpServerController {
    private final SftpServerRepository repo;
    private final SftpService sftpService;

    public SftpServerController(SftpServerRepository repo, SftpService sftpService) {
        this.repo = repo;
        this.sftpService = sftpService;
    }

    @GetMapping
    public List<SftpServerConfigDto> list() {
        return repo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public SftpServerConfigDto get(@PathVariable Long id) {
        return repo.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public SftpServerConfigDto create(@RequestBody CreateSftpServerRequest req) {
        if (repo.existsByName(req.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate server name");
        }
        SftpServerConfig cfg = new SftpServerConfig();
        cfg.setName(req.getName());
        cfg.setProtocol(req.getProtocol());
        cfg.setHost(req.getHost());
        cfg.setPort(req.getPort());
        cfg.setBasePath(req.getBasePath());
        cfg.setUsername(req.getUsername());
        cfg.setPassword(req.getPassword());
        cfg.setTimeout(req.getTimeout());
        cfg.setPassiveMode(req.getPassiveMode());
        cfg = repo.save(cfg);
        return toDto(cfg);
    }

    @PutMapping("/{id}")
    public SftpServerConfigDto update(@PathVariable Long id,
                                      @RequestBody CreateSftpServerRequest req) {
        SftpServerConfig cfg = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        cfg.setName(req.getName());
        cfg.setProtocol(req.getProtocol());
        cfg.setHost(req.getHost());
        cfg.setPort(req.getPort());
        cfg.setBasePath(req.getBasePath());
        cfg.setUsername(req.getUsername());
        cfg.setPassword(req.getPassword());
        cfg.setTimeout(req.getTimeout());
        cfg.setPassiveMode(req.getPassiveMode());
        cfg = repo.save(cfg);
        return toDto(cfg);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        repo.deleteById(id);
    }

    @GetMapping("/{id}/test")
    public Map<String, Boolean> test(@PathVariable Long id) {
        SftpServerConfig cfg = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean ok = sftpService.testConnection(cfg);
        return Map.of("ok", ok);
    }

    private SftpServerConfigDto toDto(SftpServerConfig cfg) {
        SftpServerConfigDto dto = new SftpServerConfigDto();
        dto.setId(cfg.getId());
        dto.setName(cfg.getName());
        dto.setProtocol(cfg.getProtocol());
        dto.setHost(cfg.getHost());
        dto.setPort(cfg.getPort());
        dto.setBasePath(cfg.getBasePath());
        dto.setUsername(cfg.getUsername());
        dto.setTimeout(cfg.getTimeout());
        dto.setPassiveMode(cfg.getPassiveMode());
        return dto;
    }
}