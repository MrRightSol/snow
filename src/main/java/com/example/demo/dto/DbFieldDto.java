package com.example.demo.dto;

/**
 * DTO for database field/column metadata.
 */
public class DbFieldDto {
    private Long id;
    private String columnName;
    private String dataType;
    private int ordinalPosition;
    private Integer length;
    private String columnConstraints;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }
    public Integer getLength() {
        return length;
    }
    public void setLength(Integer length) {
        this.length = length;
    }
    public String getColumnConstraints() {
        return columnConstraints;
    }
    public void setColumnConstraints(String columnConstraints) {
        this.columnConstraints = columnConstraints;
    }
}