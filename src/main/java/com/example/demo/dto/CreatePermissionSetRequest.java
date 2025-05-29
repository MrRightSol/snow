package com.example.demo.dto;

/**
 * Payload for creating a new PermissionSet.
 */
public class CreatePermissionSetRequest {
    private String name;
    // list of user IDs associated with this PermissionSet
    private java.util.List<Long> userIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public java.util.List<Long> getUserIds() {
        return userIds;
    }
    public void setUserIds(java.util.List<Long> userIds) {
        this.userIds = userIds;
    }
}