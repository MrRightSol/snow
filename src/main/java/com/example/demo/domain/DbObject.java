package com.example.demo.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a database object (table or view) for a given DataSource.
 */
@Entity
@Table(name = "db_object")
public class DbObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "datasource_config_id")
    private DataSourceConfig dataSourceConfig;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    @Column(name = "object_type", nullable = false)
    private String objectType;

    @OneToMany(mappedBy = "dbObject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DbField> fields = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DataSourceConfig getDataSourceConfig() {
        return dataSourceConfig;
    }

    public void setDataSourceConfig(DataSourceConfig dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public List<DbField> getFields() {
        return fields;
    }

    public void setFields(List<DbField> fields) {
        this.fields = fields;
    }
}