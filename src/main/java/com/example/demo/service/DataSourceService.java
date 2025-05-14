package com.example.demo.service;

import com.example.demo.domain.DataSourceConfig;
import com.example.demo.dto.CreateDataSourceRequest;
import com.example.demo.dto.DataSourceConfigDto;
import com.example.demo.repository.DataSourceConfigRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages dynamic DataSource configurations and JdbcTemplates.
 */
@Service
public class DataSourceService {
    private final DataSourceConfigRepository repository;
    private final Map<Long, JdbcTemplate> templates = new ConcurrentHashMap<>();

    public DataSourceService(DataSourceConfigRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        repository.findAll().forEach(this::initTemplate);
    }

    /**
     * Create a new DataSource config and initialize its pool.
     */
    public DataSourceConfigDto create(CreateDataSourceRequest req) {
        DataSourceConfig cfg = new DataSourceConfig();
        cfg.setName(req.getName());
        cfg.setUrl(req.getUrl());
        cfg.setUsername(req.getUsername());
        cfg.setPassword(req.getPassword());
        cfg.setDriverClassName(req.getDriverClassName());
        cfg.setInstanceName(req.getInstanceName());
        cfg = repository.save(cfg);
        initTemplate(cfg);
        return toDto(cfg);
    }

    /**
     * List all DataSource configs.
     */
    public List<DataSourceConfigDto> list() {
        return repository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Test the connection for a given config.
     */
    public boolean testConnection(Long id) {
        JdbcTemplate jt = getJdbcTemplate(id);
        try {
            jt.execute("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retrieve the JdbcTemplate for the given config id.
     */
    public JdbcTemplate getJdbcTemplate(Long id) {
        JdbcTemplate jt = templates.get(id);
        if (jt == null) {
            throw new RuntimeException("No DataSource configured for id: " + id);
        }
        return jt;
    }

    private void initTemplate(DataSourceConfig cfg) {
        HikariDataSource ds = new HikariDataSource();
        // Build JDBC URL, inserting instanceName for SQL Server if provided
        String url = cfg.getUrl();
        String instance = cfg.getInstanceName();
        if (instance != null && !instance.isBlank()
                && cfg.getDriverClassName().contains("sqlserver")) {
            String prefix = "jdbc:sqlserver://";
            if (url.startsWith(prefix)) {
                int idx = prefix.length();
                int semi = url.indexOf(';', idx);
                String hostPort = semi > 0 ? url.substring(idx, semi) : url.substring(idx);
                String tail = semi > 0 ? url.substring(semi) : "";
                String newHostPort;
                int colon = hostPort.indexOf(':');
                if (colon > 0) {
                    String host = hostPort.substring(0, colon);
                    String port = hostPort.substring(colon + 1);
                    newHostPort = host + "\\" + instance + ":" + port;
                } else {
                    newHostPort = hostPort + "\\" + instance;
                }
                url = prefix + newHostPort + tail;
            }
        }
        ds.setJdbcUrl(url);
        ds.setUsername(cfg.getUsername());
        ds.setPassword(cfg.getPassword());
        ds.setDriverClassName(cfg.getDriverClassName());
        templates.put(cfg.getId(), new JdbcTemplate(ds));
    }

    private DataSourceConfigDto toDto(DataSourceConfig cfg) {
        DataSourceConfigDto dto = new DataSourceConfigDto();
        dto.setId(cfg.getId());
        dto.setName(cfg.getName());
        dto.setUrl(cfg.getUrl());
        dto.setUsername(cfg.getUsername());
        dto.setDriverClassName(cfg.getDriverClassName());
        dto.setInstanceName(cfg.getInstanceName());
        return dto;
    }
}