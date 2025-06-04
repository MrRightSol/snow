package com.example.demo.repository;

import com.example.demo.domain.DbCredential;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DbCredentialRepository extends JpaRepository<DbCredential, Long> {
    Optional<DbCredential> findByUsernameAndPassword(String username, String password);
    List<DbCredential> findByDataSource_Id(Long dataSourceId);
}