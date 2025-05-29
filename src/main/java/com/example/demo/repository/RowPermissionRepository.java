package com.example.demo.repository;

import com.example.demo.domain.RowPermission;
import com.example.demo.domain.PermissionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RowPermissionRepository extends JpaRepository<RowPermission, Long> {
    /**
     * Single-set row permission lookup (existing).
     */
    Optional<RowPermission> findByPermissionSetAndObjectName(PermissionSet permissionSet, String objectName);
    /**
     * Multi-set row permission lookup.
     */
    java.util.List<RowPermission> findByPermissionSetInAndObjectName(
            java.util.List<com.example.demo.domain.PermissionSet> permissionSets,
            String objectName);
}