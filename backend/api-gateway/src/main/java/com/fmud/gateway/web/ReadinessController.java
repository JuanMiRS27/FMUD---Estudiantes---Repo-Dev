package com.fmud.gateway.web;

import com.fmud.gateway.configuration.GatewayProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class ReadinessController {
    private final GatewayProperties properties;
    private final ObjectMapper mapper;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    public ReadinessController(GatewayProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.mapper = mapper;
    }

    @GetMapping("/api/health/ready")
    public ResponseEntity<Map<String, String>> ready() {
        // Wake both services concurrently; do not send credentials while they start.
        var auth = check(properties.authServiceUrl());
        var students = check(properties.studentServiceUrl());
        boolean available = auth.join() & students.join();
        return ResponseEntity.status(available ? 200 : 503)
                .header("Cache-Control", "no-store")
                .body(Map.of("status", available ? "UP" : "STARTING"));
    }

    private CompletableFuture<Boolean> check(String baseUrl) {
        var request = HttpRequest.newBuilder(URI.create(baseUrl + "/actuator/health"))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .GET().build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 200
                        && "UP".equals(mapper.readTree(response.body()).path("status").asText()))
                .exceptionally(error -> false);
    }
}
