package com.tuxlogic.shiftiq.platform.shared.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Root and Health Check Controller.
 * Provides public endpoints for cloud health checks (e.g. Render, AWS, Kubernetes).
 */
@RestController
@Tag(name = "Health", description = "Application health and status endpoints")
public class RootController {

    @Operation(summary = "Root ping endpoint", description = "Verifies the application is running and accessible")
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "app", "ShiftIQ Platform API",
                "docs", "/swagger-ui.html"
        ));
    }

    @Operation(summary = "Health check endpoint", description = "Standard health check for deployment orchestrators")
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP"
        ));
    }
}
