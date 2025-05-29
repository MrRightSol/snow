package com.example.demo.domain;

import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.ConstraintMode;

/**
 * Represents a column/field of a DbObject.
 */
@Entity
@Table(name = "GW_db_field")
public class DbField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "db_object_id",
                foreignKey = @jakarta.persistence.ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private DbObject dbObject;

    @Column(name = "column_name", nullable = false)
    private String columnName;

    @Column(name = "data_type", nullable = false)
    private String dataType;

    @Column(name = "ordinal_position", nullable = false)
    private int ordinalPosition;

    @Column(name = "field_length")
    private Integer length;

    @Column(name = "column_constraints", length = 2000)
    private String columnConstraints;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DbObject getDbObject() {
        return dbObject;
    }

    public void setDbObject(DbObject dbObject) {
        this.dbObject = dbObject;
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