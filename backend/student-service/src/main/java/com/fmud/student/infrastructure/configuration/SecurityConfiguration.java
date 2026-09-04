package com.fmud.student.infrastructure.configuration;

import com.fmud.student.infrastructure.security.JwtTokenValidator;
import com.fmud.student.infrastructure.exception.ApiErrorResponse;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Configuration
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenValidator validator, ObjectMapper mapper) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/health", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "SECRETARIO")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, mapper, request.getRequestURI(), HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "La sesion no es valida o expiro."))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(response, mapper, request.getRequestURI(), HttpStatus.FORBIDDEN, "ACCESS_DENIED", "No tiene permisos para realizar esta operacion."))
                )
                .addFilterBefore(new com.fmud.student.infrastructure.security.JwtAuthenticationFilter(validator), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void writeError(HttpServletResponse response, ObjectMapper mapper, String path, HttpStatus status, String code, String message)
            throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), new ApiErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), code, message, path, List.of()));
    }
}
