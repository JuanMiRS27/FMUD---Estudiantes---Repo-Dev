package com.fmud.gateway.web;

import com.fmud.gateway.configuration.GatewayProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ReadinessControllerTest {
    HttpServer upstream;
    ExecutorService executor;
    ReadinessController controller;

    @BeforeEach
    void setup() throws Exception {
        upstream = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        executor = Executors.newCachedThreadPool();
        upstream.setExecutor(executor);
        upstream.start();
        String url = "http://127.0.0.1:" + upstream.getAddress().getPort();
        controller = new ReadinessController(new GatewayProperties(url + "/auth", url + "/students", 180, List.of()), new ObjectMapper());
    }

    @AfterEach
    void cleanup() { upstream.stop(0); executor.shutdownNow(); }

    @Test
    void checksBothServicesConcurrently() {
        CountDownLatch both = new CountDownLatch(2);
        upstream.createContext("/", exchange -> {
            both.countDown();
            boolean parallel;
            try { parallel = both.await(2, TimeUnit.SECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); parallel = false; }
            byte[] body = (parallel ? "{\"status\":\"UP\"}" : "{}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) { output.write(body); }
        });
        var result = controller.ready();
        assertEquals(200, result.getStatusCode().value());
        assertEquals("UP", result.getBody().get("status"));
    }

    @Test
    void doesNotReportReadyWhenOneServiceReturnsRenderLoadingHtml() {
        upstream.createContext("/", exchange -> {
            byte[] body = (exchange.getRequestURI().getPath().startsWith("/auth")
                    ? "{\"status\":\"UP\"}" : "<html>Starting</html>").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) { output.write(body); }
        });
        var result = controller.ready();
        assertEquals(503, result.getStatusCode().value());
        assertEquals("STARTING", result.getBody().get("status"));
    }
}
