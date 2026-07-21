package com.fmud.gateway.web;

import com.fmud.gateway.configuration.GatewayProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
public class GatewayController {
    private final RestClient restClient;
    private final GatewayProperties properties;

    public GatewayController(RestClient restClient, GatewayProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<String> login(@RequestBody String body) {
        return forwardPost(properties.authServiceUrl() + "/api/auth/login", body, null, "/api/auth/login");
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<String> me(HttpServletRequest request) {
        return forwardGet(properties.authServiceUrl() + "/api/auth/me", request.getHeader(HttpHeaders.AUTHORIZATION), "/api/auth/me");
    }

    @GetMapping("/api/auth/admin-check")
    public ResponseEntity<String> adminCheck(HttpServletRequest request) {
        return forwardGet(properties.authServiceUrl() + "/api/auth/admin-check", request.getHeader(HttpHeaders.AUTHORIZATION), "/api/auth/admin-check");
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "api-gateway");
    }

    private ResponseEntity<String> forwardPost(String url, String body, String authorization, String path) {
        try {
            RestClient.RequestBodySpec spec = restClient.post().uri(url).contentType(MediaType.APPLICATION_JSON);
            if (authorization != null) {
                spec.header(HttpHeaders.AUTHORIZATION, authorization);
            }
            return toJsonResponse(spec.body(body).retrieve().toEntity(String.class));
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).contentType(MediaType.APPLICATION_JSON).body(ex.getResponseBodyAsString());
        } catch (ResourceAccessException ex) {
            return serviceUnavailable(path);
        }
    }

    private ResponseEntity<String> forwardGet(String url, String authorization, String path) {
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authorization == null ? "" : authorization)
                    .retrieve()
                    .toEntity(String.class);
            return toJsonResponse(response);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).contentType(MediaType.APPLICATION_JSON).body(ex.getResponseBodyAsString());
        } catch (ResourceAccessException ex) {
            return serviceUnavailable(path);
        }
    }

    private ResponseEntity<String> toJsonResponse(ResponseEntity<String> upstreamResponse) {
        return ResponseEntity
                .status(upstreamResponse.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(upstreamResponse.getBody());
    }

    private ResponseEntity<String> serviceUnavailable(String path) {
        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                "SERVICE_UNAVAILABLE",
                "Este módulo no se encuentra disponible temporalmente. Intenta nuevamente más tarde.",
                path,
                List.of()
        );
        String json = """
                {"timestamp":"%s","status":503,"error":"Service Unavailable","code":"SERVICE_UNAVAILABLE","message":"%s","path":"%s","details":[]}
                """.formatted(error.timestamp(), error.message(), error.path()).trim();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(MediaType.APPLICATION_JSON).body(json);
    }
}
