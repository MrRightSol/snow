package com.example.demo.service;

import com.example.demo.domain.ApiKey;
import com.example.demo.domain.FieldPermission;
import com.example.demo.domain.PermissionSet;
import com.example.demo.domain.QueryLog;
import com.example.demo.domain.RowPermission;
import com.example.demo.dto.QueryResponse;
import com.example.demo.repository.ApiKeyRepository;
import com.example.demo.repository.FieldPermissionRepository;
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
    private final JdbcTemplate defaultJdbcTemplate;
    private final QueryLogRepository queryLogRepository;
    private final DataSourceService dataSourceService;

    public QueryService(ApiKeyRepository apiKeyRepository,
                        FieldPermissionRepository fieldPermissionRepository,
                        RowPermissionRepository rowPermissionRepository,
                        DataSourceService dataSourceService,
                        JdbcTemplate jdbcTemplate,
                        QueryLogRepository queryLogRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.fieldPermissionRepository = fieldPermissionRepository;
        this.rowPermissionRepository = rowPermissionRepository;
        this.dataSourceService = dataSourceService;
        this.defaultJdbcTemplate = jdbcTemplate;
        this.queryLogRepository = queryLogRepository;
    }

    @Transactional
    public QueryResponse executeQuery(String apiKeyValue, String rawSql, Long serverId) {
        ApiKey apiKey = apiKeyRepository.findByKey(apiKeyValue)
                .orElseThrow(() -> new RuntimeException("Invalid API key"));
        if (apiKey.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("API key expired");
        }
        PermissionSet ps = apiKey.getPermissionSet();
        try {
            String normalized = rawSql.trim().replaceAll("\\s+", " ");
            String normalizedUpper = normalized.toUpperCase(Locale.ROOT);
            String selectPrefix = "SELECT * FROM ";
            if (!normalizedUpper.startsWith(selectPrefix)) {
                throw new RuntimeException("Only simple SELECT * FROM <table> statements are supported");
            }
            String tableName = normalized.substring(selectPrefix.length()).trim();
            // legacy regex parsing removed
            FieldPermission fp = fieldPermissionRepository
                    .findByPermissionSetAndObjectName(ps, tableName)
                    .orElseThrow(() -> new RuntimeException("No field permission for table: " + tableName));
            List<String> allowedFields = Arrays.asList(fp.getFields().split(","));
            StringBuilder adjusted = new StringBuilder();
            adjusted.append("SELECT ").append(String.join(", ", allowedFields))
                    .append(" FROM ").append(tableName);
            Optional<RowPermission> rpOpt = rowPermissionRepository
                    .findByPermissionSetAndObjectName(ps, tableName);
            if (rpOpt.isPresent()) {
                adjusted.append(" WHERE ").append(rpOpt.get().getExpression());
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