package com.example.demo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "field_permission")
public class FieldPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "permission_set_id")
    private PermissionSet permissionSet;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    @Column(name = "fields", nullable = false, length = 1000)
    private String fields;

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

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }
}