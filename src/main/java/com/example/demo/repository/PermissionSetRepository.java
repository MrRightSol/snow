package com.example.demo.repository;

import com.example.demo.domain.PermissionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionSetRepository extends JpaRepository<PermissionSet, Long> {
    /**
     * Find a PermissionSet by its unique name.
     */
    java.util.Optional<com.example.demo.domain.PermissionSet> findByName(String name);
    /**
     * Find all PermissionSets that include the given userId.
     */
    java.util.List<com.example.demo.domain.PermissionSet> findByUserIdsContains(Long userId);
}