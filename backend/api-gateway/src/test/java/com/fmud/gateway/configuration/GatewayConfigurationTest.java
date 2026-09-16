package com.fmud.gateway.configuration;

import com.fmud.gateway.web.GatewayController;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewayConfigurationTest {
    @Test
    void preservesInvalidCredentialsResponseFromAuth() throws Exception {
        String error = "{\"code\":\"INVALID_CREDENTIALS\",\"message\":\"Credenciales incorrectas.\"}";
        HttpServer upstream = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        upstream.createContext("/api/auth/login", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] body = error.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, body.length);
            try (var output = exchange.getResponseBody()) { output.write(body); }
        });
        upstream.start();
        try {
            String url = "http://127.0.0.1:" + upstream.getAddress().getPort();
            var properties = new GatewayProperties(url, url, 2, List.of());
            var client = new GatewayConfiguration().restClient(properties);
            var gateway = new GatewayController(client, properties);

            var response = gateway.login("{\"email\":\"test@example.com\",\"password\":\"invalid\"}");

            assertEquals(401, response.getStatusCode().value());
            assertEquals(error, response.getBody());
        } finally {
            upstream.stop(0);
        }
    }
}
