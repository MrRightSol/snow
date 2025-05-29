package com.example.demo.dto;

/**
 * DTO representing a PermissionSet.
 */
public class PermissionSetDto {
    private Long id;
    private String name;
    // user IDs associated with this PermissionSet
    private java.util.List<Long> userIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public java.util.List<Long> getUserIds() {
        return userIds;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setUserIds(java.util.List<Long> userIds) {
        this.userIds = userIds;
    }
}