package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * Request to create an SFTP/FTP credential.
 */
public class CreateSftpCredentialRequest {
    private Long permissionSetId;
    private LocalDateTime expiresAt;

    public Long getPermissionSetId() { return permissionSetId; }
    public void setPermissionSetId(Long permissionSetId) { this.permissionSetId = permissionSetId; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}