package com.example.demo.controller;

import com.example.demo.domain.ApiKey;
import com.example.demo.domain.PermissionSet;
import com.example.demo.domain.SftpServerConfig;
import com.example.demo.dto.QueryResponse;
import com.example.demo.repository.ApiKeyRepository;
import com.example.demo.repository.PermissionSetRepository;
import com.example.demo.repository.SftpPermissionRepository;
import com.example.demo.repository.SftpServerRepository;
import com.example.demo.service.SftpService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

/**
 * REST endpoints for SFTP file download and upload with permission enforcement.
 */
@RestController
@RequestMapping("/api/sftp-servers/{serverId}")
public class SftpFileController {
    private final ApiKeyRepository apiKeyRepo;
    private final PermissionSetRepository psRepo;
    private final SftpPermissionRepository permRepo;
    private final SftpServerRepository serverRepo;
    private final SftpService sftpService;

    public SftpFileController(ApiKeyRepository apiKeyRepo,
                              PermissionSetRepository psRepo,
                              SftpPermissionRepository permRepo,
                              SftpServerRepository serverRepo,
                              SftpService sftpService) {
        this.apiKeyRepo = apiKeyRepo;
        this.psRepo = psRepo;
        this.permRepo = permRepo;
        this.serverRepo = serverRepo;
        this.sftpService = sftpService;
    }

    /**
     * Download a file from the remote SFTP/FTP server if READ permission is granted.
     */
    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(
            @PathVariable Long serverId,
            @RequestHeader("X-API-KEY") String apiKeyValue,
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam("path") String path) {
        // Validate API key
        ApiKey ak = apiKeyRepo.findByKey(apiKeyValue)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid API key"));
        if (ak.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(FORBIDDEN, "API key expired");
        }
        // Validate server
        SftpServerConfig server = serverRepo.findById(serverId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Server not found"));
        // Collect user permission sets
        List<PermissionSet> sets = psRepo.findByUserIdsContains(userId);
        if (sets.isEmpty()) {
            throw new ResponseStatusException(FORBIDDEN, "No permission sets for user");
        }
        // Collect SFTP permissions for READ
        boolean allowed = permRepo.findByPermissionSetInAndServer_Id(sets, serverId).stream()
                .anyMatch(p -> p.getOperations().contains("READ") && matches(p.getPathPattern(), path));
        if (!allowed) {
            throw new ResponseStatusException(FORBIDDEN, "No READ permission for path: " + path);
        }
        StreamingResponseBody stream = out -> {
            try {
                sftpService.downloadTo(server, path, out);
            } catch (Exception e) {
                throw new java.io.IOException("Download failed", e);
            }
        };
        String filename = path.contains("/") ? path.substring(path.lastIndexOf('/')+1) : path;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
    }

    /**
     * Upload a file to the remote SFTP/FTP server if WRITE permission is granted.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @PathVariable Long serverId,
            @RequestHeader("X-API-KEY") String apiKeyValue,
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam("path") String path,
            @RequestBody byte[] data) {
        // Validate API key
        ApiKey ak = apiKeyRepo.findByKey(apiKeyValue)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid API key"));
        if (ak.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(FORBIDDEN, "API key expired");
        }
        // Validate server
        SftpServerConfig server = serverRepo.findById(serverId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Server not found"));
        // Collect permission sets
        List<PermissionSet> sets = psRepo.findByUserIdsContains(userId);
        if (sets.isEmpty()) {
            throw new ResponseStatusException(FORBIDDEN, "No permission sets for user");
        }
        // Check WRITE permission
        boolean allowed = permRepo.findByPermissionSetInAndServer_Id(sets, serverId).stream()
                .anyMatch(p -> p.getOperations().contains("WRITE") && matches(p.getPathPattern(), path));
        if (!allowed) {
            throw new ResponseStatusException(FORBIDDEN, "No WRITE permission for path: " + path);
        }
        // Perform upload
        try (java.io.InputStream in = new java.io.ByteArrayInputStream(data)) {
            sftpService.uploadFrom(server, path, in);
        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Upload failed: " + e.getMessage(), e);
        }
        return ResponseEntity.ok(Map.of("status", "uploaded"));
    }

    private boolean matches(String pattern, String path) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(pattern);
    }
}