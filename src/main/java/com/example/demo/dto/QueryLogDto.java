package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * DTO representing a QueryLog entry.
 */
public class QueryLogDto {
    private Long id;
    private Long apiKeyId;
    private String rawSql;
    private String adjustedSql;
    private int rowsReturned;
    private LocalDateTime executedAt;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApiKeyId() {
        return apiKeyId;
    }

    public void setApiKeyId(Long apiKeyId) {
        this.apiKeyId = apiKeyId;
    }

    public String getRawSql() {
        return rawSql;
    }

    public void setRawSql(String rawSql) {
        this.rawSql = rawSql;
    }

    public String getAdjustedSql() {
        return adjustedSql;
    }

    public void setAdjustedSql(String adjustedSql) {
        this.adjustedSql = adjustedSql;
    }

    public int getRowsReturned() {
        return rowsReturned;
    }

    public void setRowsReturned(int rowsReturned) {
        this.rowsReturned = rowsReturned;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}