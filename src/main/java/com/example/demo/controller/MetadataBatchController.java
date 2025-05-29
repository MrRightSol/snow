package com.example.demo.controller;

import com.example.demo.dto.BatchImportRequest;
import com.example.demo.dto.DbObjectDto;
import com.example.demo.service.MetadataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoint for batch metadata import.
 */
@RestController
@RequestMapping("/api/metadata")
public class MetadataBatchController {
    private final MetadataService metadataService;

    public MetadataBatchController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * Batch import metadata for multiple data sources and object types.
     * Example objectTypes: ["TABLE","VIEW","SP"].
     */
    @PostMapping("/import")
    public Map<Long, List<DbObjectDto>> importBatch(@RequestBody BatchImportRequest req) {
        return metadataService.importMetadataBatch(
                req.getDataSourceIds(),
                req.getObjectTypes()
        );
    }
}