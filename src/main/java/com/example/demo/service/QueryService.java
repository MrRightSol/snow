package com.example.demo.service;

import com.example.demo.domain.ApiKey;
import com.example.demo.domain.FieldPermission;
import com.example.demo.domain.PermissionSet;
import com.example.demo.domain.QueryLog;
import com.example.demo.domain.RowPermission;
import com.example.demo.dto.QueryResponse;
import com.example.demo.repository.ApiKeyRepository;
import com.example.demo.repository.FieldPermissionRepository;
import com.example.demo.repository.DbObjectRepository;
import com.example.demo.repository.PermissionSetRepository;
import com.example.demo.repository.QueryLogRepository;
import com.example.demo.repository.RowPermissionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;

@Service
public class QueryService {
    private final ApiKeyRepository apiKeyRepository;
    private final FieldPermissionRepository fieldPermissionRepository;
    private final RowPermissionRepository rowPermissionRepository;
    private final DbObjectRepository dbObjectRepository;
    private final PermissionSetRepository permissionSetRepository;
    private final JdbcTemplate defaultJdbcTemplate;
    private final QueryLogRepository queryLogRepository;
    private final DataSourceService dataSourceService;

    public QueryService(ApiKeyRepository apiKeyRepository,
                        FieldPermissionRepository fieldPermissionRepository,
                        RowPermissionRepository rowPermissionRepository,
                        PermissionSetRepository permissionSetRepository,
                        DbObjectRepository dbObjectRepository,
                        DataSourceService dataSourceService,
                        JdbcTemplate jdbcTemplate,
                        QueryLogRepository queryLogRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.fieldPermissionRepository = fieldPermissionRepository;
        this.rowPermissionRepository = rowPermissionRepository;
        this.permissionSetRepository = permissionSetRepository;
        this.dbObjectRepository = dbObjectRepository;
        this.dataSourceService = dataSourceService;
        this.defaultJdbcTemplate = jdbcTemplate;
        this.queryLogRepository = queryLogRepository;
    }

    // Query prefix strategy: no physical prefix for external tables
    @Transactional
    public QueryResponse executeQuery(String apiKeyValue,
                                      Long userId,
                                      String rawSql,
                                      Long serverId) {
        // Validate API key
        ApiKey apiKey = apiKeyRepository.findByKey(apiKeyValue)
                .orElseThrow(() -> new RuntimeException("Invalid API key"));
        if (apiKey.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("API key expired");
        }
        // Determine all permission sets for this user
        java.util.List<PermissionSet> sets =
                permissionSetRepository.findByUserIdsContains(userId);
        if (sets.isEmpty()) {
            throw new RuntimeException("No permission sets for user: " + userId);
        }
        try {
            String normalized = rawSql.trim().replaceAll("\\s+", " ");
            String normalizedUpper = normalized.toUpperCase(Locale.ROOT);
            String selectPrefix = "SELECT * FROM ";
            if (!normalizedUpper.startsWith(selectPrefix)) {
                throw new RuntimeException("Only simple SELECT * FROM <table> statements are supported");
            }
            String tableName = normalized.substring(selectPrefix.length()).trim();
            // If querying an external DB server, ensure the object was imported
            if (serverId != null) {
                boolean imported = dbObjectRepository
                        .existsByDataSourceConfig_IdAndObjectName(serverId, tableName);
                if (!imported) {
                    throw new RuntimeException("Object not found: " + tableName);
                }
            }
            // Consolidate allowed fields across all permission sets
            java.util.List<FieldPermission> fps =
                fieldPermissionRepository.findByPermissionSetInAndObjectName(sets, tableName);
            if (fps.isEmpty()) {
                throw new RuntimeException("No field permissions for table: " + tableName);
            }
            java.util.LinkedHashSet<String> allowedFields = new java.util.LinkedHashSet<>();
            for (FieldPermission fp : fps) {
                for (String col : fp.getFields().split(",")) {
                    allowedFields.add(col.trim());
                }
            }
            StringBuilder adjusted = new StringBuilder();
            adjusted.append("SELECT ")
                    .append(String.join(", ", allowedFields))
                    .append(" FROM ")
                    .append(tableName);
            // Consolidate row-level predicates across all permission sets (OR)
            java.util.List<RowPermission> rps =
                rowPermissionRepository.findByPermissionSetInAndObjectName(sets, tableName);
            if (!rps.isEmpty()) {
                String rls = rps.stream()
                    .map(RowPermission::getExpression)
                    .collect(java.util.stream.Collectors.joining(" OR "));
                adjusted.append(" WHERE ").append(rls);
            }
            String adjustedSql = adjusted.toString();
            // choose appropriate JdbcTemplate: dynamic if serverId provided, otherwise default
            JdbcTemplate jt = (serverId != null)
                    ? dataSourceService.getJdbcTemplate(serverId)
                    : defaultJdbcTemplate;
            List<Map<String, Object>> rows = jt.queryForList(adjustedSql);
            int rowsReturned = rows.size();
            QueryLog log = new QueryLog();
            log.setApiKey(apiKey);
            log.setRawSql(rawSql);
            log.setAdjustedSql(adjustedSql);
            log.setRowsReturned(rowsReturned);
            log.setExecutedAt(LocalDateTime.now());
            log.setStatus("SUCCESS");
            queryLogRepository.save(log);
            return new QueryResponse(rawSql, adjustedSql, rows, rowsReturned);
        } catch (Exception e) {
            throw new RuntimeException("Error processing query: " + e.getMessage(), e);
        }
    }
}