package com.example.demo.dto;

import java.util.List;

/**
 * DTO representing an SFTP/FTP path permission.
 */
public class SftpPermissionDto {
    private Long id;
    private Long permissionSetId;
    private Long serverId;
    private String pathPattern;
    private List<String> operations;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPermissionSetId() { return permissionSetId; }
    public void setPermissionSetId(Long permissionSetId) { this.permissionSetId = permissionSetId; }

    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }

    public String getPathPattern() { return pathPattern; }
    public void setPathPattern(String pathPattern) { this.pathPattern = pathPattern; }

    public List<String> getOperations() { return operations; }
    public void setOperations(List<String> operations) { this.operations = operations; }
}