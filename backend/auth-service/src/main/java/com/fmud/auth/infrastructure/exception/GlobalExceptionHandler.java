package com.fmud.auth.infrastructure.exception;

import com.fmud.auth.domain.exception.AuthenticationFailedException;
import com.fmud.auth.domain.exception.UserDisabledException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationFailedException.class)
    ResponseEntity<ApiErrorResponse> authenticationFailed(AuthenticationFailedException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(UserDisabledException.class)
    ResponseEntity<ApiErrorResponse> userDisabled(UserDisabledException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "USER_DISABLED", ex.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .toList();
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Los datos enviados no son válidos.", request.getRequestURI(), details);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> internal(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Ocurrió un error interno no controlado.", request.getRequestURI(), List.of());
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String code, String message, String path, List<String> details) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path,
                details
        ));
    }
}
