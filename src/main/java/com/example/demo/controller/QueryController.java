package com.example.demo.controller;

import com.example.demo.dto.QueryRequest;
import com.example.demo.dto.QueryResponse;
import com.example.demo.service.QueryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class QueryController {
    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/query")
    public QueryResponse execute(@RequestHeader("X-API-KEY") String apiKey,
                                 @RequestBody QueryRequest request) {
        return queryService.executeQuery(apiKey,
                                         request.getSql(),
                                         request.getServerId());
    }
}