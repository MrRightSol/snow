package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * DTO representing a DbCredential.
 */
public class DbCredentialDto {
    private Long id;
    private Long dataSourceId;
    private Long permissionSetId;
    private String username;
    private LocalDateTime expiresAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDataSourceId() { return dataSourceId; }
    public void setDataSourceId(Long dataSourceId) { this.dataSourceId = dataSourceId; }

    public Long getPermissionSetId() { return permissionSetId; }
    public void setPermissionSetId(Long permissionSetId) { this.permissionSetId = permissionSetId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}