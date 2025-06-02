package com.example.demo.dto;

/**
 * Payload for creating a new FieldPermission.
 */
public class CreateFieldPermissionRequest {
    private String objectName;
    private java.util.List<String> fields;


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