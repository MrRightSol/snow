package com.example.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
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

    @Column(name = "expression", length = 2000)
    @Deprecated
    private String expression;
    
    @Column(name = "field_name", nullable = false)
    private String fieldName;
    
    @Column(name = "operator", nullable = false)
    private String operator;
    
    @Column(name = "value", nullable = false)
    private String value;

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

    /** @deprecated use structured filter fields */
    @Deprecated
    public String getExpression() {
        return expression;
    }

    /** @deprecated use setFieldName/operator/value */
    @Deprecated
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}