package com.example.demo.controller;

import com.example.demo.domain.QueryLog;
import com.example.demo.dto.QueryLogDto;
import com.example.demo.repository.QueryLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoint for querying execution logs.
 */
@RestController
@RequestMapping("/api/logs")
public class QueryLogController {
    private final QueryLogRepository logRepo;

    public QueryLogController(QueryLogRepository logRepo) {
        this.logRepo = logRepo;
    }

    @GetMapping
    public List<QueryLogDto> listLogs() {
        return logRepo.findAll().stream().map(log -> {
            QueryLogDto dto = new QueryLogDto();
            dto.setId(log.getId());
            dto.setApiKeyId(log.getApiKey().getId());
            dto.setRawSql(log.getRawSql());
            dto.setAdjustedSql(log.getAdjustedSql());
            dto.setRowsReturned(log.getRowsReturned());
            dto.setExecutedAt(log.getExecutedAt());
            dto.setStatus(log.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }
}