package com.example.demo.repository;

import com.example.demo.domain.DbObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Repository for DbObject.
 */
@Repository
public interface DbObjectRepository extends JpaRepository<DbObject, Long> {
    List<DbObject> findByDataSourceConfig_Id(Long dataSourceConfigId);

    @Modifying
    @Transactional
    void deleteByDataSourceConfig_Id(Long dataSourceConfigId);
    /**
     * Checks if a DbObject with the given name exists for a DataSource.
     */
    boolean existsByDataSourceConfig_IdAndObjectName(Long dataSourceConfigId, String objectName);
}