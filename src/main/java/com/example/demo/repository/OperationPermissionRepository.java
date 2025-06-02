package com.example.demo.repository;

import com.example.demo.domain.OperationPermission;
import com.example.demo.domain.PermissionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for operation-level permissions per PermissionSet.
 */
@Repository
public interface OperationPermissionRepository extends JpaRepository<OperationPermission, Long> {
    Optional<OperationPermission> findByPermissionSetAndObjectName(PermissionSet permissionSet, String objectName);
    List<OperationPermission> findByPermissionSetInAndObjectName(List<PermissionSet> permissionSets, String objectName);
    // Fetch all operation permissions for a single set
    List<OperationPermission> findByPermissionSet(PermissionSet permissionSet);
}