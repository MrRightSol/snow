package com.example.demo.dto;

/**
 * DTO representing a FieldPermission.
 */
public class FieldPermissionDto {
    private Long id;
    private Long permissionSetId;
    private String objectName;
    private java.util.List<String> fields;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPermissionSetId() {
        return permissionSetId;
    }

    public void setPermissionSetId(Long permissionSetId) {
        this.permissionSetId = permissionSetId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public java.util.List<String> getFields() {
        return fields;
    }

    public void setFields(java.util.List<String> fields) {
        this.fields = fields;
    }
}