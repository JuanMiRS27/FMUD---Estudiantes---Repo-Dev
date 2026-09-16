package com.fmud.gateway.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.net.http.HttpClient;

@Configuration
@EnableConfigurationProperties({GatewayProperties.class, JwtProperties.class})
public class GatewayConfiguration {
    @Bean
    RestClient restClient(GatewayProperties properties) {
        // HttpURLConnection discards the body of some 401 responses to streamed POSTs.
        // Keep Auth's error JSON so the login can display the actual failure.
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.timeoutSeconds()))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.timeoutSeconds()));
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
