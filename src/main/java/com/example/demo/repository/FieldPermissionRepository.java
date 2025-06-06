package com.example.demo.repository;

import com.example.demo.domain.FieldPermission;
import com.example.demo.domain.PermissionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FieldPermissionRepository extends JpaRepository<FieldPermission, Long> {
    /**
     * Single-set field permission lookup (existing).
     */
    Optional<FieldPermission> findByPermissionSetAndObjectName(PermissionSet permissionSet, String objectName);
    /**
     * Multi-set field permission lookup.
     */
    java.util.List<FieldPermission> findByPermissionSetInAndObjectName(
            java.util.List<PermissionSet> permissionSets,
            String objectName);
}