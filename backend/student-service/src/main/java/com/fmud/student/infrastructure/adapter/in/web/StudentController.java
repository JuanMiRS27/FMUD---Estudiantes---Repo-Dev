package com.fmud.student.infrastructure.adapter.in.web;

import com.fmud.student.application.command.ActorCommand;
import com.fmud.student.application.command.StudentCommand;
import com.fmud.student.application.dto.FileResourceDto;
import com.fmud.student.application.dto.PageResponse;
import com.fmud.student.application.dto.ResumeDto;
import com.fmud.student.application.dto.StudentDto;
import com.fmud.student.application.port.in.StudentResumeUseCase;
import com.fmud.student.domain.model.StudentStatus;
import com.fmud.student.infrastructure.security.JwtPrincipal;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentResumeUseCase useCase;

    public StudentController(StudentResumeUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public StudentDto create(@Valid @ModelAttribute StudentRequest request, @RequestPart(required = false) MultipartFile photo, Authentication authentication) {
        return useCase.create(toCommand(request, photo), actor(authentication));
    }

    @GetMapping
    public PageResponse<StudentDto> list(@RequestParam(required = false) String search,
                                         @RequestParam(required = false) StudentStatus status,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "12") int size) {
        return useCase.list(search, status, PageRequest.of(page, size, Sort.by("lastName").ascending().and(Sort.by("firstName").ascending())));
    }

    @GetMapping("/{id}")
    public StudentDto get(@PathVariable UUID id) {
        return useCase.get(id);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudentDto update(@PathVariable UUID id, @Valid @ModelAttribute StudentRequest request, @RequestPart(required = false) MultipartFile photo, Authentication authentication) {
        return useCase.update(id, toCommand(request, photo), actor(authentication));
    }

    @PatchMapping("/{id}/status")
    public StudentDto status(@PathVariable UUID id, @RequestBody StatusRequest request, Authentication authentication) {
        return useCase.changeStatus(id, request.status(), actor(authentication));
    }

    @GetMapping("/{id}/resume")
    public ResumeDto resume(@PathVariable UUID id) {
        return useCase.resume(id);
    }

    @GetMapping("/{id}/history")
    public java.util.List<com.fmud.student.application.dto.HistoryEventDto> history(@PathVariable UUID id) {
        return useCase.history(id);
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<Resource> photo(@PathVariable UUID id) {
        return file(useCase.photo(id));
    }

    private StudentCommand toCommand(StudentRequest request, MultipartFile photo) {
        return new StudentCommand(request.firstName(), request.lastName(), request.documentNumber(), request.birthDate(), request.birthPlace(),
                request.address(), request.phone(), request.email(), request.status(), photo);
    }

    private ActorCommand actor(Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return new ActorCommand(principal.id(), principal.name(), principal.role());
    }

    private ResponseEntity<Resource> file(FileResourceDto file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.size())
                .body(file.resource());
    }

    public record StatusRequest(StudentStatus status) {
    }
}
