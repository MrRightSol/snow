package com.example.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Credentials for accessing an SFTP/FTP server, tied to a PermissionSet.
 */
@Entity
@Table(name = "GW_sftp_credential")
public class SftpCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "server_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private SftpServerConfig server;

    @ManyToOne(optional = false)
    @JoinColumn(name = "permission_set_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PermissionSet permissionSet;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SftpServerConfig getServer() { return server; }
    public void setServer(SftpServerConfig server) { this.server = server; }

    public PermissionSet getPermissionSet() { return permissionSet; }
    public void setPermissionSet(PermissionSet permissionSet) { this.permissionSet = permissionSet; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}