package com.fmud.auth.infrastructure.adapter.in.web;

import com.fmud.auth.application.command.LoginCommand;
import com.fmud.auth.application.dto.AuthenticatedUserDto;
import com.fmud.auth.application.dto.LoginResultDto;
import com.fmud.auth.application.port.in.GetCurrentUserUseCase;
import com.fmud.auth.application.port.in.LoginUseCase;
import com.fmud.auth.infrastructure.security.JwtPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public AuthController(LoginUseCase loginUseCase, GetCurrentUserUseCase getCurrentUserUseCase) {
        this.loginUseCase = loginUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResultDto> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUseCase.login(new LoginCommand(request.email(), request.password())));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserDto> me(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(getCurrentUserUseCase.getById(principal.id()));
    }

    @GetMapping("/admin-check")
    public ResponseEntity<Void> adminOnly() {
        return ResponseEntity.noContent().build();
    }
}
