package com.example.demo.controller;

import com.example.demo.dto.DbFieldDto;
import com.example.demo.dto.DbObjectDto;
import com.example.demo.service.MetadataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for importing and retrieving database metadata.
 */
@RestController
@RequestMapping("/api/datasources/{dsId}")
public class MetadataController {
    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * Import tables/views and columns from the specified DataSource.
     */
    @PostMapping("/import")
    public List<DbObjectDto> importMetadata(@PathVariable Long dsId) {
        return metadataService.importMetadata(dsId);
    }

    /**
     * List imported database objects for a DataSource.
     */
    @GetMapping("/objects")
    public List<DbObjectDto> listObjects(@PathVariable Long dsId) {
        return metadataService.listObjects(dsId);
    }

    /**
     * List fields/columns for a given database object.
     */
    @GetMapping("/objects/{objectId}/fields")
    public List<DbFieldDto> listFields(@PathVariable Long dsId,
                                        @PathVariable Long objectId) {
        return metadataService.listFields(dsId, objectId);
    }
}