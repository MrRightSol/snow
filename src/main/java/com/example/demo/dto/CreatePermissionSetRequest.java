package com.example.demo.dto;

/**
 * Payload for creating a new PermissionSet.
 */
public class CreatePermissionSetRequest {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}