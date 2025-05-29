package com.example.demo.service;

import com.example.demo.domain.SftpObject;
import com.example.demo.domain.SftpServerConfig;
import com.example.demo.dto.SftpObjectDto;
import com.example.demo.repository.SftpObjectRepository;
import com.example.demo.repository.SftpServerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Imports and retrieves metadata (file/directory) from SFTP/FTP servers.
 */
@Service
public class SftpMetadataService {
    private final SftpServerRepository serverRepo;
    private final SftpObjectRepository objectRepo;
    private final SftpService sftpService;

    public SftpMetadataService(SftpServerRepository serverRepo,
                               SftpObjectRepository objectRepo,
                               SftpService sftpService) {
        this.serverRepo = serverRepo;
        this.objectRepo = objectRepo;
        this.sftpService = sftpService;
    }

    /**
     * Recursively imports metadata from the given server and path.
     */
    @Transactional
    public List<SftpObjectDto> importMetadata(Long serverId, String path) {
        SftpServerConfig server = serverRepo.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found: " + serverId));
        // clear existing metadata
        objectRepo.deleteByServer_Id(serverId);
        List<SftpObjectDto> result = new ArrayList<>();
        // recursive walk
        walk(server, path, result);
        return result;
    }

    private void walk(SftpServerConfig server, String path, List<SftpObjectDto> out) {
        try {
            List<SftpObjectDto> entries = sftpService.listDirectory(server, path);
            for (SftpObjectDto dto : entries) {
                // persist
                SftpObject obj = new SftpObject();
                obj.setServer(server);
                obj.setPath(dto.getPath());
                obj.setName(dto.getName());
                obj.setObjectType(dto.getObjectType());
                obj.setSize(dto.getSize());
                obj.setLastModified(dto.getLastModified());
                obj.setPermissions(dto.getPermissions());
                objectRepo.save(obj);
                out.add(dto);
                if ("DIR".equals(dto.getObjectType())) {
                    walk(server, dto.getPath(), out);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import metadata from " + path, e);
        }
    }
}