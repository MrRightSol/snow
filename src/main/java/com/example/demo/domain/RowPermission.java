package com.example.demo.domain;

import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.ConstraintMode;

@Entity
@Table(name = "GW_row_permission")
public class RowPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "permission_set_id",
                foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PermissionSet permissionSet;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    @Column(name = "expression", nullable = false, length = 2000)
    private String expression;

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

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}