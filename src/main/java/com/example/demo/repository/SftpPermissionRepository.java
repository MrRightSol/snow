package com.example.demo.repository;

import com.example.demo.domain.SftpPermission;
import com.example.demo.domain.PermissionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SftpPermissionRepository extends JpaRepository<SftpPermission, Long> {
    List<SftpPermission> findByPermissionSet(PermissionSet permissionSet);
    List<SftpPermission> findByPermissionSetInAndServer_Id(List<PermissionSet> permissionSets, Long serverId);
}