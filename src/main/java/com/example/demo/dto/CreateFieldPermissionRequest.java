package com.example.demo.dto;

/**
 * Payload for creating a new FieldPermission.
 */
public class CreateFieldPermissionRequest {
    private Long permissionSetId;
    private String objectName;
    private String fields;

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

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }
}