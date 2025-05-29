package com.example.demo.domain;

import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.ConstraintMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "GW_api_key")
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_key", nullable = false, unique = true)
    private String key;

    @ManyToOne(optional = false)
    @JoinColumn(name = "permission_set_id",
                foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PermissionSet permissionSet;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public PermissionSet getPermissionSet() {
        return permissionSet;
    }

    public void setPermissionSet(PermissionSet permissionSet) {
        this.permissionSet = permissionSet;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}