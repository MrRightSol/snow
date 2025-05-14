package com.example.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "query_log")
public class QueryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "api_key_id")
    private ApiKey apiKey;

    @Column(name = "raw_sql", nullable = false, length = 2000)
    private String rawSql;

    @Column(name = "adjusted_sql", nullable = false, length = 2000)
    private String adjustedSql;

    @Column(name = "rows_returned", nullable = false)
    private int rowsReturned;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "status", nullable = false)
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApiKey getApiKey() {
        return apiKey;
    }

    public void setApiKey(ApiKey apiKey) {
        this.apiKey = apiKey;
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