package com.fmud.gateway.web;

import com.fmud.gateway.configuration.GatewayProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class GatewayControllerTest {
    private HttpServer upstream;
    private GatewayController gateway;
    private static final byte[] JSON = "{\"content\":[],\"totalElements\":0}".getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void startUpstream() throws Exception {
        upstream = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        upstream.start();
        String url = "http://127.0.0.1:" + upstream.getAddress().getPort();
        gateway = new GatewayController(RestClient.create(), new GatewayProperties(url, url, 1, List.of()));
    }

    @AfterEach
    void stopUpstream() {
        upstream.stop(0);
    }

    @Test
    void browserCompressionDoesNotCorruptStudentJson() throws Exception {
        AtomicReference<String> encoding = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> query = new AtomicReference<>();
        upstream.createContext("/api/students", exchange -> {
            encoding.set(exchange.getRequestHeaders().getFirst("Accept-Encoding"));
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            query.set(exchange.getRequestURI().getQuery());
            byte[] body = JSON;
            if (!"identity".equals(encoding.get())) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                try (GZIPOutputStream gzip = new GZIPOutputStream(buffer)) { gzip.write(JSON); }
                body = buffer.toByteArray();
                exchange.getResponseHeaders().set("Content-Encoding", "gzip");
            }
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) { output.write(body); }
        });
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/students");
        request.setQueryString("page=0&size=10");
        request.addHeader("Accept-Encoding", "gzip, deflate, br");
        request.addHeader("Authorization", "Bearer test-token");

        var response = gateway.students(request);

        assertEquals(200, response.getStatusCode().value());
        assertArrayEquals(JSON, response.getBody());
        assertEquals("identity", encoding.get());
        assertEquals("Bearer test-token", authorization.get());
        assertEquals("page=0&size=10", query.get());
    }

    @Test
    void rawProxyUsesConfiguredTimeout() throws Exception {
        upstream.createContext("/api/students", exchange -> {
            try { Thread.sleep(1600); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            try {
                exchange.sendResponseHeaders(200, JSON.length);
                try (var output = exchange.getResponseBody()) { output.write(JSON); }
            } catch (java.io.IOException ignored) { exchange.close(); }
        });
        var response = gateway.students(new MockHttpServletRequest("GET", "/api/students"));
        assertEquals(503, response.getStatusCode().value());
    }
}
