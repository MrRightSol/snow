package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * DTO for SFTP/FTP object metadata.
 */
public class SftpObjectDto {
    private Long id;
    private String path;
    private String name;
    private String objectType;
    private Long size;
    private LocalDateTime lastModified;
    private String permissions;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
}