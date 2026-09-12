package com.fmud.gateway.web;

import com.fmud.gateway.configuration.GatewayProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class GatewayController {
    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);
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

    @RequestMapping({"/api/students", "/api/students/**"})
    public ResponseEntity<byte[]> students(HttpServletRequest request) throws IOException {
        return forwardRaw(request, properties.studentServiceUrl());
    }

    @RequestMapping({"/api/users", "/api/users/**"})
    public ResponseEntity<byte[]> users(HttpServletRequest request) throws IOException {
        return forwardRaw(request, properties.authServiceUrl());
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
        } catch (RestClientException ex) {
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
        } catch (RestClientException ex) {
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
        } catch (RestClientException ex) {
            return serviceUnavailable(path);
        }
    }

    private ResponseEntity<byte[]> forwardMultipartHttpClient(String url, Map<String, String> params, Map<String, MultipartFile> files,
                                                              String authorization, String path, String method) {
        String boundary = "----fmud-" + UUID.randomUUID();
        try {
            byte[] body = multipartBody(params, files, boundary);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE + "; boundary=" + boundary);
            if (authorization != null) {
                builder.header(HttpHeaders.AUTHORIZATION, authorization);
            }
            HttpResponse<byte[]> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(properties.timeoutSeconds()))
                    .build()
                    .send(builder.method(method, HttpRequest.BodyPublishers.ofByteArray(body)).build(), HttpResponse.BodyHandlers.ofByteArray());
            HttpHeaders responseHeaders = new HttpHeaders();
            response.headers().firstValue(HttpHeaders.CONTENT_TYPE)
                    .ifPresent(value -> responseHeaders.setContentType(MediaType.parseMediaType(value)));
            response.headers().firstValue(HttpHeaders.CONTENT_DISPOSITION)
                    .ifPresent(value -> responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, value));
            return new ResponseEntity<>(response.body(), responseHeaders, HttpStatus.valueOf(response.statusCode()));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(serviceUnavailableJson(path).getBytes(StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(serviceUnavailableJson(path).getBytes(StandardCharsets.UTF_8));
        }
    }

    private byte[] multipartBody(Map<String, String> params, Map<String, MultipartFile> files, String boundary) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Map.Entry<String, String> param : params.entrySet()) {
            writeTextPart(out, boundary, param.getKey(), param.getValue());
        }
        for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
            MultipartFile file = entry.getValue();
            if (file != null && !file.isEmpty()) {
                writeFilePart(out, boundary, entry.getKey(), file);
            }
        }
        out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    private void writeTextPart(ByteArrayOutputStream out, String boundary, String name, String value) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void writeFilePart(ByteArrayOutputStream out, String boundary, String name, MultipartFile file) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + safeHeaderValue(file.getOriginalFilename()) + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + (file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType()) + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(file.getBytes());
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private String safeHeaderValue(String value) {
        return value == null ? "file" : value.replace("\r", "_").replace("\n", "_").replace("\"", "_");
    }

    private ResponseEntity<byte[]> forwardRaw(HttpServletRequest request, String upstreamBaseUrl) throws IOException {
        String requestUri = request.getRequestURI();
        String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        String url = upstreamBaseUrl + requestUri + query;
        byte[] body = request.getInputStream().readAllBytes();
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
                    .header(HttpHeaders.ACCEPT_ENCODING, "identity");
            Collections.list(request.getHeaderNames()).forEach(name -> {
                if (!name.equalsIgnoreCase(HttpHeaders.HOST)
                        && !name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)
                        && !name.equalsIgnoreCase(HttpHeaders.CONNECTION)
                        && !name.equalsIgnoreCase(HttpHeaders.EXPECT)
                        && !name.equalsIgnoreCase(HttpHeaders.UPGRADE)
                        && !name.equalsIgnoreCase(HttpHeaders.ACCEPT_ENCODING)) {
                    Collections.list(request.getHeaders(name)).forEach(value -> builder.header(name, value));
                }
            });
            HttpRequest.BodyPublisher publisher = body.length == 0 ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(body);
            HttpResponse<byte[]> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(properties.timeoutSeconds()))
                    .build()
                    .send(builder.method(request.getMethod(), publisher).build(), HttpResponse.BodyHandlers.ofByteArray());
            HttpHeaders responseHeaders = new HttpHeaders();
            response.headers().firstValue(HttpHeaders.CONTENT_TYPE)
                    .ifPresent(value -> responseHeaders.setContentType(MediaType.parseMediaType(value)));
            response.headers().firstValue(HttpHeaders.CONTENT_DISPOSITION)
                    .ifPresent(value -> responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, value));
            response.headers().firstValue(HttpHeaders.CONTENT_ENCODING)
                    .ifPresent(value -> responseHeaders.set(HttpHeaders.CONTENT_ENCODING, value));
            return new ResponseEntity<>(response.body(), responseHeaders, HttpStatus.valueOf(response.statusCode()));
        } catch (RestClientResponseException ex) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(ex.getResponseBodyAsByteArray(), responseHeaders, ex.getStatusCode());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Gateway raw proxy interrupted for {}", requestUri, ex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(serviceUnavailableJson(requestUri).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException ex) {
            log.warn("Gateway raw proxy I/O error for {}", requestUri, ex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(serviceUnavailableJson(requestUri).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (RuntimeException ex) {
            log.warn("Gateway raw proxy runtime error for {}", requestUri, ex);
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

    private Map<String, String> studentParams(StudentMultipartRequest form) {
        Map<String, String> params = new java.util.LinkedHashMap<>();
        params.put("firstName", form.firstName());
        params.put("lastName", form.lastName());
        params.put("documentNumber", form.documentNumber());
        params.put("birthDate", form.birthDate());
        params.put("birthPlace", form.birthPlace());
        params.put("address", form.address());
        params.put("phone", form.phone());
        params.put("email", form.email());
        params.put("status", form.status());
        return params;
    }

    private Map<String, String> multipartParams(HttpServletRequest request) throws ServletException, IOException {
        Map<String, String> params = new java.util.LinkedHashMap<>();
        for (Part part : request.getParts()) {
            if (part.getSubmittedFileName() == null) {
                params.put(part.getName(), new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return params;
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

    private record StudentMultipartRequest(
            String firstName,
            String lastName,
            String documentNumber,
            String birthDate,
            String birthPlace,
            String address,
            String phone,
            String email,
            String status
    ) {
    }
}
