package com.example.demo.dto;

import java.util.List;
import java.util.Map;

public class QueryResponse {
    private String originalSql;
    private String adjustedSql;
    private List<Map<String, Object>> rows;
    private int rowsReturned;

    public QueryResponse(String originalSql, String adjustedSql, List<Map<String, Object>> rows, int rowsReturned) {
        this.originalSql = originalSql;
        this.adjustedSql = adjustedSql;
        this.rows = rows;
        this.rowsReturned = rowsReturned;
    }

    public String getOriginalSql() {
        return originalSql;
    }

    public String getAdjustedSql() {
        return adjustedSql;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public int getRowsReturned() {
        return rowsReturned;
    }
}