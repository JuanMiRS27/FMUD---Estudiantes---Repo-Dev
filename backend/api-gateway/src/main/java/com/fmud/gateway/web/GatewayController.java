package com.fmud.gateway.web;

import com.fmud.gateway.configuration.GatewayProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
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

    @PostMapping(path = "/api/students", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createStudent(@RequestParam Map<String, String> params,
                                                @RequestPart(required = false) MultipartFile photo,
                                                HttpServletRequest request) {
        Map<String, MultipartFile> files = new java.util.HashMap<>();
        files.put("photo", photo);
        return forwardMultipart(properties.studentServiceUrl() + "/api/students", params, files, request.getHeader(HttpHeaders.AUTHORIZATION), "/api/students");
    }

    @PutMapping(path = "/api/students/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateStudent(@org.springframework.web.bind.annotation.PathVariable String id,
                                                @RequestParam Map<String, String> params,
                                                @RequestPart(required = false) MultipartFile photo,
                                                HttpServletRequest request) {
        Map<String, MultipartFile> files = new java.util.HashMap<>();
        files.put("photo", photo);
        return forwardMultipart(properties.studentServiceUrl() + "/api/students/" + id, params, files, request.getHeader(HttpHeaders.AUTHORIZATION), "/api/students/" + id, HttpMethod.PUT);
    }

    @PostMapping(path = "/api/students/{studentId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> attachDocument(@org.springframework.web.bind.annotation.PathVariable String studentId,
                                                 @RequestParam Map<String, String> params,
                                                 @RequestPart MultipartFile file,
                                                 HttpServletRequest request) {
        return forwardMultipart(properties.studentServiceUrl() + "/api/students/" + studentId + "/documents", params, Map.of("file", file), request.getHeader(HttpHeaders.AUTHORIZATION), "/api/students/" + studentId + "/documents");
    }

    @PutMapping(path = "/api/students/{studentId}/documents/{documentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> replaceDocument(@org.springframework.web.bind.annotation.PathVariable String studentId,
                                                  @org.springframework.web.bind.annotation.PathVariable String documentId,
                                                  @RequestParam Map<String, String> params,
                                                  @RequestPart MultipartFile file,
                                                  HttpServletRequest request) {
        return forwardMultipart(properties.studentServiceUrl() + "/api/students/" + studentId + "/documents/" + documentId, params, Map.of("file", file),
                request.getHeader(HttpHeaders.AUTHORIZATION), "/api/students/" + studentId + "/documents/" + documentId, HttpMethod.PUT);
    }

    @RequestMapping("/api/students/**")
    public ResponseEntity<byte[]> students(HttpServletRequest request) throws IOException {
        return forwardRaw(request, properties.studentServiceUrl());
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

    private ResponseEntity<String> forwardMultipart(String url, Map<String, String> params, Map<String, MultipartFile> files, String authorization, String path) {
        return forwardMultipart(url, params, files, authorization, path, HttpMethod.POST);
    }

    private ResponseEntity<String> forwardMultipart(String url, Map<String, String> params, Map<String, MultipartFile> files, String authorization, String path, HttpMethod method) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);
        files.forEach((name, file) -> {
            if (file != null && !file.isEmpty()) {
                try {
                    body.add(name, filePart(file));
                } catch (IOException ex) {
                    throw new IllegalArgumentException("No fue posible leer el archivo.");
                }
            }
        });
        try {
            RestClient.RequestBodySpec spec = restClient.method(method).uri(url).contentType(MediaType.MULTIPART_FORM_DATA);
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

    private ResponseEntity<byte[]> forwardRaw(HttpServletRequest request, String upstreamBaseUrl) throws IOException {
        String requestUri = request.getRequestURI();
        String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        String url = upstreamBaseUrl + requestUri + query;
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(name -> {
            if (!name.equalsIgnoreCase(HttpHeaders.HOST) && !name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                headers.put(name, Collections.list(request.getHeaders(name)));
            }
        });
        byte[] body = request.getInputStream().readAllBytes();
        try {
            ResponseEntity<byte[]> response = restClient.method(HttpMethod.valueOf(request.getMethod()))
                    .uri(url)
                    .headers(out -> out.addAll(headers))
                    .body(body)
                    .retrieve()
                    .toEntity(byte[].class);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(response.getHeaders().getContentType());
            responseHeaders.setContentDisposition(response.getHeaders().getContentDisposition());
            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
        } catch (RestClientResponseException ex) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(ex.getResponseBodyAsByteArray(), responseHeaders, ex.getStatusCode());
        } catch (ResourceAccessException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(serviceUnavailableJson(requestUri).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private ResponseEntity<String> toJsonResponse(ResponseEntity<String> upstreamResponse) {
        return ResponseEntity
                .status(upstreamResponse.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(upstreamResponse.getBody());
    }

    private ResponseEntity<String> serviceUnavailable(String path) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(MediaType.APPLICATION_JSON).body(serviceUnavailableJson(path));
    }

    private String serviceUnavailableJson(String path) {
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
        return json;
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        private NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename == null ? "file" : filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }

    private HttpEntity<NamedByteArrayResource> filePart(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        if (file.getContentType() != null) {
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        }
        return new HttpEntity<>(new NamedByteArrayResource(file.getBytes(), file.getOriginalFilename()), headers);
    }
}
