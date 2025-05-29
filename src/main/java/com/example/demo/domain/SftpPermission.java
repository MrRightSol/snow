package com.example.demo.domain;

import jakarta.persistence.*;

/**
 * Row-level and path-level permissions for SFTP/FTP servers per PermissionSet.
 */
@Entity
@Table(name = "GW_sftp_permission")
public class SftpPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "permission_set_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PermissionSet permissionSet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "server_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private SftpServerConfig server;

    @Column(name = "path_pattern", nullable = false, length = 2000)
    private String pathPattern;

    @Column(name = "operations", nullable = false)
    private String operations; // comma-separated READ,WRITE

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public PermissionSet getPermissionSet() {
        return permissionSet;
    }
    public void setPermissionSet(PermissionSet permissionSet) {
        this.permissionSet = permissionSet;
    }

    public SftpServerConfig getServer() {
        return server;
    }
    public void setServer(SftpServerConfig server) {
        this.server = server;
    }

    public String getPathPattern() {
        return pathPattern;
    }
    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public String getOperations() {
        return operations;
    }
    public void setOperations(String operations) {
        this.operations = operations;
    }
}