package com.fmud.student.infrastructure.adapter.in.web;

import com.fmud.student.application.command.EnrollmentCommand;
import com.fmud.student.application.dto.EnrollmentDto;
import com.fmud.student.application.port.in.EnrollmentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students/{studentId}/enrollments")
public class EnrollmentController {
    private final EnrollmentUseCase useCase;

    public EnrollmentController(EnrollmentUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentDto create(@PathVariable UUID studentId, @Valid @RequestBody EnrollmentRequest request) {
        return useCase.create(studentId, toCommand(request));
    }

    @GetMapping
    public List<EnrollmentDto> list(@PathVariable UUID studentId) {
        return useCase.list(studentId);
    }

    @PutMapping("/{enrollmentId}")
    public EnrollmentDto update(@PathVariable UUID studentId, @PathVariable UUID enrollmentId, @Valid @RequestBody EnrollmentRequest request) {
        return useCase.update(studentId, enrollmentId, toCommand(request));
    }

    private EnrollmentCommand toCommand(EnrollmentRequest request) {
        return new EnrollmentCommand(request.periodCode(), request.program(), request.status());
    }
}
