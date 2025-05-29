package com.example.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a file or directory on an external SFTP/FTP server.
 */
@Entity
@Table(name = "GW_sftp_object")
public class SftpObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "server_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private SftpServerConfig server;

    @Column(name = "path", nullable = false, length = 2000)
    private String path;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "object_type", nullable = false)
    private String objectType; // FILE or DIR

    @Column(name = "size")
    private Long size;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Column(name = "permissions")
    private String permissions;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SftpServerConfig getServer() { return server; }
    public void setServer(SftpServerConfig server) { this.server = server; }

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