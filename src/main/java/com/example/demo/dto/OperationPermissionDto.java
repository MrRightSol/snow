package com.example.demo.dto;

import java.util.List;

/**
 * DTO representing an OperationPermission.
 */
public class OperationPermissionDto {
    private Long id;
    private Long permissionSetId;
    private String objectName;
    private List<String> operations;

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

    public List<String> getOperations() {
        return operations;
    }
    public void setOperations(List<String> operations) {
        this.operations = operations;
    }
}