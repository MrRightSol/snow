package com.example.demo.repository;

import com.example.demo.domain.RowPermission;
import com.example.demo.domain.PermissionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RowPermissionRepository extends JpaRepository<RowPermission, Long> {
    Optional<RowPermission> findByPermissionSetAndObjectName(PermissionSet permissionSet, String objectName);
}