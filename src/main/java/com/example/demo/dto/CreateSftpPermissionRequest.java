package com.example.demo.dto;

import java.util.List;

/**
 * Payload for granting SFTP/FTP path permissions to a PermissionSet.
 */
public class CreateSftpPermissionRequest {
    private Long serverId;
    private String pathPattern;
    private List<String> operations; // e.g. ["READ","WRITE"]

    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }

    public String getPathPattern() { return pathPattern; }
    public void setPathPattern(String pathPattern) { this.pathPattern = pathPattern; }

    public List<String> getOperations() { return operations; }
    public void setOperations(List<String> operations) { this.operations = operations; }
}