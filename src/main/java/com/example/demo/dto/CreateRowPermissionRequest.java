package com.example.demo.dto;

/**
 * Payload for creating a new RowPermission.
 */
public class CreateRowPermissionRequest {
    private Long permissionSetId;
    private String objectName;
    private String expression;

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

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}