package com.example.demo.service;

import com.example.demo.domain.DataSourceConfig;
import com.example.demo.domain.DbField;
import com.example.demo.domain.DbObject;
import com.example.demo.dto.DbFieldDto;
import com.example.demo.dto.DbObjectDto;
import com.example.demo.repository.DataSourceConfigRepository;
import com.example.demo.repository.DbFieldRepository;
import com.example.demo.repository.DbObjectRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service to import and retrieve database metadata (objects and fields).
 */
@Service
public class MetadataService {
    private final DataSourceConfigRepository dsConfigRepo;
    private final DbObjectRepository dbObjectRepo;
    private final DbFieldRepository dbFieldRepo;
    private final DataSourceService dataSourceService;

    public MetadataService(DataSourceConfigRepository dsConfigRepo,
                           DbObjectRepository dbObjectRepo,
                           DbFieldRepository dbFieldRepo,
                           DataSourceService dataSourceService) {
        this.dsConfigRepo = dsConfigRepo;
        this.dbObjectRepo = dbObjectRepo;
        this.dbFieldRepo = dbFieldRepo;
        this.dataSourceService = dataSourceService;
    }

    /**
     * Imports metadata (tables and views) and their columns from the configured DataSource.
     * Clears any existing metadata for that DataSource.
     * @param dataSourceId The DataSourceConfig id
     * @return List of imported objects with field counts
     */
    @Transactional
    public List<DbObjectDto> importMetadata(Long dataSourceId) {
        DataSourceConfig cfg = dsConfigRepo.findById(dataSourceId)
                .orElseThrow(() -> new RuntimeException("DataSource not found: " + dataSourceId));
        JdbcTemplate jt = dataSourceService.getJdbcTemplate(dataSourceId);
        // Clear existing metadata
        dbFieldRepo.deleteByDbObject_DataSourceConfig_Id(dataSourceId);
        dbObjectRepo.deleteByDataSourceConfig_Id(dataSourceId);
        // Fetch tables and views
        String tblSql = "SELECT TABLE_NAME, TABLE_TYPE FROM INFORMATION_SCHEMA.TABLES"
                + " WHERE TABLE_TYPE IN ('TABLE','VIEW')";
        List<Map<String, Object>> tables = jt.queryForList(tblSql);
        List<DbObjectDto> result = new ArrayList<>();
        for (Map<String, Object> row : tables) {
            String name = (String) row.get("TABLE_NAME");
            String type = (String) row.get("TABLE_TYPE");
            DbObject obj = new DbObject();
            obj.setDataSourceConfig(cfg);
            obj.setObjectName(name);
            obj.setObjectType(type);
            obj = dbObjectRepo.save(obj);
            // Fetch columns for each object
            String colSql = "SELECT COLUMN_NAME, ORDINAL_POSITION, DATA_TYPE"
                    + " FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ?"
                    + " ORDER BY ORDINAL_POSITION";
            List<Map<String, Object>> cols = jt.queryForList(colSql, name);
            for (Map<String, Object> col : cols) {
                String colName = (String) col.get("COLUMN_NAME");
                int pos = ((Number) col.get("ORDINAL_POSITION")).intValue();
                String dataType = (String) col.get("DATA_TYPE");
                DbField field = new DbField();
                field.setDbObject(obj);
                field.setColumnName(colName);
                field.setDataType(dataType);
                field.setOrdinalPosition(pos);
                dbFieldRepo.save(field);
            }
            // Build DTO
            DbObjectDto dto = new DbObjectDto();
            dto.setId(obj.getId());
            dto.setObjectName(name);
            dto.setObjectType(type);
            dto.setFieldCount(cols.size());
            result.add(dto);
        }
        return result;
    }

    /**
     * List imported database objects for a DataSource.
     */
    public List<DbObjectDto> listObjects(Long dataSourceId) {
        dsConfigRepo.findById(dataSourceId)
                .orElseThrow(() -> new RuntimeException("DataSource not found: " + dataSourceId));
        List<DbObject> objs = dbObjectRepo.findByDataSourceConfig_Id(dataSourceId);
        List<DbObjectDto> result = new ArrayList<>();
        for (DbObject obj : objs) {
            DbObjectDto dto = new DbObjectDto();
            dto.setId(obj.getId());
            dto.setObjectName(obj.getObjectName());
            dto.setObjectType(obj.getObjectType());
            long count = dbFieldRepo.countByDbObject(obj);
            dto.setFieldCount((int) count);
            result.add(dto);
        }
        return result;
    }

    /**
     * List columns/fields for a given DbObject.
     */
    public List<DbFieldDto> listFields(Long dataSourceId, Long objectId) {
        // ensure DataSource exists
        dsConfigRepo.findById(dataSourceId)
                .orElseThrow(() -> new RuntimeException("DataSource not found: " + dataSourceId));
        DbObject obj = dbObjectRepo.findById(objectId)
                .orElseThrow(() -> new RuntimeException("DbObject not found: " + objectId));
        List<DbField> fields = dbFieldRepo.findByDbObject(obj);
        List<DbFieldDto> result = new ArrayList<>();
        for (DbField f : fields) {
            DbFieldDto dto = new DbFieldDto();
            dto.setId(f.getId());
            dto.setColumnName(f.getColumnName());
            dto.setDataType(f.getDataType());
            dto.setOrdinalPosition(f.getOrdinalPosition());
            result.add(dto);
        }
        return result;
    }
}