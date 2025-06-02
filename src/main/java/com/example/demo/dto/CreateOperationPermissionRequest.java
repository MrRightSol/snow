package com.example.demo.dto;

import java.util.List;

/**
 * Request payload to grant operations on a database object.
 */
public class CreateOperationPermissionRequest {
    private String objectName;
    private List<String> operations;

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