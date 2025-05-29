package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * DTO for SFTP/FTP credential.
 */
public class SftpCredentialDto {
    private Long id;
    private Long serverId;
    private Long permissionSetId;
    private String username;
    private LocalDateTime expiresAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }

    public Long getPermissionSetId() { return permissionSetId; }
    public void setPermissionSetId(Long permissionSetId) { this.permissionSetId = permissionSetId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}