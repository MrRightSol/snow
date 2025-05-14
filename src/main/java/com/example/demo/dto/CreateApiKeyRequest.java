package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * Payload for creating a new ApiKey.
 */
public class CreateApiKeyRequest {
    private Long permissionSetId;
    private LocalDateTime expiresAt;

    public Long getPermissionSetId() {
        return permissionSetId;
    }

    public void setPermissionSetId(Long permissionSetId) {
        this.permissionSetId = permissionSetId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}