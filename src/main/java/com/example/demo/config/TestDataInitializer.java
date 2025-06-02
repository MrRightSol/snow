package com.example.demo.config;

import com.example.demo.domain.ApiKey;
import com.example.demo.domain.FieldPermission;
import com.example.demo.domain.PermissionSet;
import com.example.demo.repository.ApiKeyRepository;
import com.example.demo.repository.FieldPermissionRepository;
import com.example.demo.repository.PermissionSetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;

@Configuration
@Profile("default")
public class TestDataInitializer {
    @Bean
    @Transactional
    public CommandLineRunner initData(PermissionSetRepository psRepo,
                                      FieldPermissionRepository fpRepo,
                                      ApiKeyRepository akRepo,
                                      JdbcTemplate jdbcTemplate) {
        return args -> {
            // Create test_table compatible with H2 and SQL Server
            try {
                String dbName = jdbcTemplate.getDataSource()
                        .getConnection()
                        .getMetaData()
                        .getDatabaseProductName()
                        .toLowerCase();
                if (dbName.contains("sql server")) {
                    jdbcTemplate.execute(
                        "IF OBJECT_ID('test_table', 'U') IS NULL " +
                        "CREATE TABLE test_table (id INT PRIMARY KEY, name VARCHAR(100))"
                    );
                } else {
                    jdbcTemplate.execute(
                        "CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(100))"
                    );
                }
            } catch (Exception e) {
                // Fallback for non-SQLServer
                jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(100))"
                );
            }
            // Seed rows (ignore duplicate key errors)
            try {
                jdbcTemplate.update("INSERT INTO test_table(id, name) VALUES(1, 'Alice')");
            } catch (Exception ignored) { }
            try {
                jdbcTemplate.update("INSERT INTO test_table(id, name) VALUES(2, 'Bob')");
            } catch (Exception ignored) { }
            // Ensure TestSet permission set exists (and assign to userId=1)
            com.example.demo.domain.PermissionSet ps;
            java.util.Optional<com.example.demo.domain.PermissionSet> psOpt = psRepo.findByName("TestSet");
            if (psOpt.isPresent()) {
                ps = psOpt.get();
            } else {
                ps = new com.example.demo.domain.PermissionSet();
                ps.setName("TestSet");
                ps = psRepo.save(ps);
            }
            // Assign seed user (id=1) to TestSet if not already present
            if (!ps.getUserIds().contains(1L)) {
                ps.getUserIds().add(1L);
                ps = psRepo.save(ps);
            }
            // Ensure field permission exists for test_table
            if (fpRepo.findByPermissionSetAndObjectName(ps, "test_table").isEmpty()) {
                FieldPermission fp = new FieldPermission();
                fp.setPermissionSet(ps);
                fp.setObjectName("test_table");
                fp.setFields("id,name");
                fpRepo.save(fp);
            }
            // Ensure API key exists
            if (akRepo.findByKey("test-key").isEmpty()) {
                ApiKey ak = new ApiKey();
                ak.setKey("test-key");
                ak.setPermissionSet(ps);
                ak.setExpiresAt(LocalDateTime.now().plusDays(1));
                akRepo.save(ak);
            }
        };
    }
}