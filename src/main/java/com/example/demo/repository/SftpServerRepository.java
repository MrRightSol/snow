package com.example.demo.repository;

import com.example.demo.domain.SftpServerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SftpServerRepository extends JpaRepository<SftpServerConfig, Long> {
    boolean existsByName(String name);
}