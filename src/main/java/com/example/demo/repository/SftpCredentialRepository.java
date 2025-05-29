package com.example.demo.repository;

import com.example.demo.domain.SftpCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SftpCredentialRepository extends JpaRepository<SftpCredential, Long> {
    List<SftpCredential> findByServer_Id(Long serverId);
    List<SftpCredential> findByPermissionSet_Id(Long permissionSetId);
    /**
     * Lookup credential by its generated username.
     */
    java.util.Optional<com.example.demo.domain.SftpCredential> findByUsername(String username);
}