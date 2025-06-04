package com.example.demo.controller;

import com.example.demo.dto.QueryRequest;
import com.example.demo.dto.QueryResponse;
import com.example.demo.service.QueryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class QueryController {
    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/query")
    public QueryResponse execute(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey,
            @RequestHeader(value = "X-USER-ID", required = false) Long userId,
            @RequestBody QueryRequest request) {
        // API key path
        if (apiKey != null && userId != null) {
            return queryService.executeQuery(
                    apiKey,
                    userId,
                    request.getSql(),
                    request.getServerId());
        }
        // Basic Auth (DB credential) path: Authorization: Basic base64(user:pass)
        if (auth != null && auth.toLowerCase().startsWith("basic ")) {
            String base64 = auth.substring(6).trim();
            String decoded = new String(java.util.Base64.getDecoder().decode(base64), java.nio.charset.StandardCharsets.UTF_8);
            int idx = decoded.indexOf(':');
            if (idx < 0) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid Basic auth format");
            }
            String dbUser = decoded.substring(0, idx);
            String dbPass = decoded.substring(idx + 1);
            return queryService.executeQueryDb(
                    dbUser,
                    dbPass,
                    request.getSql(),
                    request.getServerId());
        }
        throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing credentials");
    }
}