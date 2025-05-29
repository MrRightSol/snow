package com.example.demo.controller;

import com.example.demo.dto.SftpObjectDto;
import com.example.demo.service.SftpMetadataService;
import com.example.demo.repository.SftpObjectRepository;
import com.example.demo.domain.SftpObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for importing and retrieving SFTP/FTP server metadata.
 */
@RestController
@RequestMapping("/api/sftp-servers/{serverId}/objects")
public class SftpMetadataController {
    private final SftpMetadataService metadataService;
    private final com.example.demo.repository.SftpObjectRepository objectRepo;

    public SftpMetadataController(SftpMetadataService metadataService,
                                  com.example.demo.repository.SftpObjectRepository objectRepo) {
        this.metadataService = metadataService;
        this.objectRepo = objectRepo;
    }

    /**
     * Recursively import files and directories from the given path.
     */
    @PostMapping("/import")
    public List<SftpObjectDto> importMetadata(
            @PathVariable Long serverId,
            @RequestParam(value = "path", defaultValue = "/") String path) {
        return metadataService.importMetadata(serverId, path);
    }

    /**
     * List imported file/directory metadata (optionally under a specific path).
     */
    @GetMapping
    public List<SftpObjectDto> listObjects(
            @PathVariable Long serverId,
            @RequestParam(value = "path", required = false) String path) {
        // List all imported objects or those under the given path prefix
        java.util.List<com.example.demo.domain.SftpObject> objs;
        if (path == null || path.isEmpty()) {
            objs = objectRepo.findByServer_Id(serverId);
        } else {
            objs = objectRepo.findByServer_IdAndPathStartingWith(serverId, path);
        }
        return objs.stream().map(obj -> {
            SftpObjectDto dto = new SftpObjectDto();
            dto.setId(obj.getId());
            dto.setPath(obj.getPath());
            dto.setName(obj.getName());
            dto.setObjectType(obj.getObjectType());
            dto.setSize(obj.getSize());
            dto.setLastModified(obj.getLastModified());
            dto.setPermissions(obj.getPermissions());
            return dto;
        }).toList();
    }
}