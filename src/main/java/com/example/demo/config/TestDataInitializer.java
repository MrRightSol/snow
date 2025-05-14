package com.example.demo.config;

import com.example.demo.domain.ApiKey;
import com.example.demo.domain.FieldPermission;
import com.example.demo.domain.PermissionSet;
import com.example.demo.repository.ApiKeyRepository;
import com.example.demo.repository.FieldPermissionRepository;
import com.example.demo.repository.PermissionSetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;

@Configuration
public class TestDataInitializer {
    @Bean
    public CommandLineRunner initData(PermissionSetRepository psRepo,
                                      FieldPermissionRepository fpRepo,
                                      ApiKeyRepository akRepo,
                                      JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS test_table(id INT PRIMARY KEY, name VARCHAR(100))"
            );
            jdbcTemplate.execute(
                "INSERT INTO test_table(id, name) VALUES(1, 'Alice'), (2, 'Bob')"
            );
            PermissionSet ps = new PermissionSet();
            ps.setName("TestSet");
            ps = psRepo.save(ps);
            FieldPermission fp = new FieldPermission();
            fp.setPermissionSet(ps);
            fp.setObjectName("test_table");
            fp.setFields("id,name");
            fpRepo.save(fp);
            ApiKey ak = new ApiKey();
            ak.setKey("test-key");
            ak.setPermissionSet(ps);
            ak.setExpiresAt(LocalDateTime.now().plusDays(1));
            akRepo.save(ak);
        };
    }
}