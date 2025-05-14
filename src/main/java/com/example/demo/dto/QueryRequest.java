package com.example.demo.dto;

public class QueryRequest {
    private String sql;
    private Long serverId;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
    public Long getServerId() {
        return serverId;
    }
    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }
}