package com.fmud.auth.infrastructure.adapter.in.web;

import com.fmud.auth.application.dto.UserDto;
import com.fmud.auth.application.usecase.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserManagementService service;

    public UserController(UserManagementService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserDto> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserRequest request) {
        return service.create(request.name(), request.email(), request.password(), request.role(), request.enabled());
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable UUID id, @Valid @RequestBody UserRequest request) {
        return service.update(id, request.name(), request.email(), request.password(), request.role(), request.enabled());
    }

    @PatchMapping("/{id}/enabled")
    public UserDto enabled(@PathVariable UUID id, @RequestBody EnabledRequest request) {
        return service.changeEnabled(id, request.enabled());
    }
}
