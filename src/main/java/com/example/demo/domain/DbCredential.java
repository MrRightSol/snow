package com.example.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Credentials for accessing a DataSource via the Snow JDBC/ODBC proxy.
 */
@Entity
@Table(name = "GW_db_credential")
public class DbCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "datasource_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private DataSourceConfig dataSource;

    @ManyToOne(optional = false)
    @JoinColumn(name = "permission_set_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PermissionSet permissionSet;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DataSourceConfig getDataSource() { return dataSource; }
    public void setDataSource(DataSourceConfig dataSource) { this.dataSource = dataSource; }

    public PermissionSet getPermissionSet() { return permissionSet; }
    public void setPermissionSet(PermissionSet permissionSet) { this.permissionSet = permissionSet; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}