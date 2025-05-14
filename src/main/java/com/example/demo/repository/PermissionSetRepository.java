package com.example.demo.repository;

import com.example.demo.domain.PermissionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionSetRepository extends JpaRepository<PermissionSet, Long> {
}