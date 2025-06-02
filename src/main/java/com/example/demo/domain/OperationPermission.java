package com.example.demo.domain;

import jakarta.persistence.*;
import jakarta.persistence.ConstraintMode;
import java.io.Serializable;

/**
 * Operation-level permissions (CRUD and schema operations) on a single object per PermissionSet.
 */
@Entity
@Table(name = "GW_operation_permission")
public class OperationPermission implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "permission_set_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PermissionSet permissionSet;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    @Column(name = "operations", nullable = false)
    private String operations; // comma-separated codes: C,R,U,D,N,E

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

    public String getOperations() {
        return operations;
    }

    public void setOperations(String operations) {
        this.operations = operations;
    }
}