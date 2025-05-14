package com.example.demo.repository;

import com.example.demo.domain.DbField;
import com.example.demo.domain.DbObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Repository for DbField.
 */
@Repository
public interface DbFieldRepository extends JpaRepository<DbField, Long> {
    List<DbField> findByDbObject(DbObject dbObject);
    long countByDbObject(DbObject dbObject);

    @Modifying
    @Transactional
    void deleteByDbObject_DataSourceConfig_Id(Long dataSourceConfigId);
}