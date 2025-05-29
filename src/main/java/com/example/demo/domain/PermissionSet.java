package com.example.demo.domain;

import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.ConstraintMode;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "GW_permission_set")
public class PermissionSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "GW_permission_set_user",
        joinColumns = @JoinColumn(
            name = "permission_set_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
        )
    )
    @Column(name = "user_id", nullable = false)
    private Set<Long> userIds = new HashSet<>();
    
    public Set<Long> getUserIds() {
        return userIds;
    }
    public void setUserIds(Set<Long> userIds) {
        this.userIds = userIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}