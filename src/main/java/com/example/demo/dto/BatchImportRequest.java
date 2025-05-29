package com.example.demo.dto;

import java.util.List;

/**
 * Request payload for batch metadata import.
 */
public class BatchImportRequest {
    private List<Long> dataSourceIds;
    private List<String> objectTypes;

    public List<Long> getDataSourceIds() {
        return dataSourceIds;
    }

    public void setDataSourceIds(List<Long> dataSourceIds) {
        this.dataSourceIds = dataSourceIds;
    }

    public List<String> getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(List<String> objectTypes) {
        this.objectTypes = objectTypes;
    }
}