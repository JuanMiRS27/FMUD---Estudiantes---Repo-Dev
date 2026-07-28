package com.fmud.student.infrastructure.adapter.in.web;

import com.fmud.student.application.command.ActorCommand;
import com.fmud.student.application.command.DocumentCommand;
import com.fmud.student.application.dto.DocumentDto;
import com.fmud.student.application.dto.FileResourceDto;
import com.fmud.student.application.port.in.StudentResumeUseCase;
import com.fmud.student.infrastructure.security.JwtPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students/{studentId}/documents")
public class DocumentController {
    private final StudentResumeUseCase useCase;

    public DocumentController(StudentResumeUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDto attach(@PathVariable UUID studentId, @RequestParam String documentType, @RequestParam String displayName,
                              @RequestParam(required = false) String description, @RequestPart MultipartFile file, Authentication authentication) {
        return useCase.attachDocument(studentId, new DocumentCommand(documentType, displayName, description, file), actor(authentication));
    }

    @GetMapping
    public List<DocumentDto> list(@PathVariable UUID studentId, @RequestParam(required = false) String type) {
        return useCase.listDocuments(studentId, type);
    }

    @GetMapping("/{documentId}")
    public DocumentDto get(@PathVariable UUID studentId, @PathVariable UUID documentId) {
        return useCase.listDocuments(studentId, null).stream()
                .filter(document -> document.id().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new com.fmud.student.infrastructure.exception.NotFoundException("El documento no existe."));
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID studentId, @PathVariable UUID documentId) {
        FileResourceDto file = useCase.downloadDocument(studentId, documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.filename()).build().toString())
                .body(file.resource());
    }

    @PutMapping(path = "/{documentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentDto replace(@PathVariable UUID studentId, @PathVariable UUID documentId, @RequestParam String documentType,
                               @RequestParam String displayName, @RequestParam(required = false) String description,
                               @RequestPart MultipartFile file, Authentication authentication) {
        return useCase.replaceDocument(studentId, documentId, new DocumentCommand(documentType, displayName, description, file), actor(authentication));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID studentId, @PathVariable UUID documentId, Authentication authentication) {
        useCase.deleteDocument(studentId, documentId, actor(authentication));
        return ResponseEntity.noContent().build();
    }

    private ActorCommand actor(Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return new ActorCommand(principal.id(), principal.name(), principal.role());
    }
}
