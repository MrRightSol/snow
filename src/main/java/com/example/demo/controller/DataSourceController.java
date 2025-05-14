package com.example.demo.controller;

import com.example.demo.dto.CreateDataSourceRequest;
import com.example.demo.dto.DataSourceConfigDto;
import com.example.demo.service.DataSourceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for managing DataSource configurations.
 */
@RestController
@RequestMapping("/api/datasources")
public class DataSourceController {
    private final DataSourceService dataSourceService;

    public DataSourceController(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    /**
     * Create a new DataSource configuration.
     */
    @PostMapping
    public DataSourceConfigDto create(@RequestBody CreateDataSourceRequest req) {
        return dataSourceService.create(req);
    }

    /**
     * List all DataSource configurations.
     */
    @GetMapping
    public List<DataSourceConfigDto> list() {
        return dataSourceService.list();
    }

    /**
     * Test connectivity for a given DataSource config.
     */
    @GetMapping("/{id}/test")
    public Map<String, Boolean> test(@PathVariable Long id) {
        boolean ok = dataSourceService.testConnection(id);
        return Map.of("ok", ok);
    }
}