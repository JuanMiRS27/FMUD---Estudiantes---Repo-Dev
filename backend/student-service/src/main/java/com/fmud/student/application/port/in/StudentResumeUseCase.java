package com.fmud.student.application.port.in;

import com.fmud.student.application.command.ActorCommand;
import com.fmud.student.application.command.DocumentCommand;
import com.fmud.student.application.command.StudentCommand;
import com.fmud.student.application.dto.*;
import com.fmud.student.domain.model.StudentStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudentResumeUseCase {
    StudentDto create(StudentCommand command, ActorCommand actor);

    StudentDto update(UUID id, StudentCommand command, ActorCommand actor);

    PageResponse<StudentDto> list(String search, StudentStatus status, Pageable pageable);

    StudentDto get(UUID id);

    StudentDto changeStatus(UUID id, StudentStatus status, ActorCommand actor);

    ResumeDto resume(UUID studentId);

    ResumeDetailsDto updateDetails(UUID studentId, ResumeDetailsDto details, ActorCommand actor);

    DocumentDto attachDocument(UUID studentId, DocumentCommand command, ActorCommand actor);

    java.util.List<DocumentDto> listDocuments(UUID studentId, String type);

    FileResourceDto downloadDocument(UUID studentId, UUID documentId);

    DocumentDto replaceDocument(UUID studentId, UUID documentId, DocumentCommand command, ActorCommand actor);

    void deleteDocument(UUID studentId, UUID documentId, ActorCommand actor);

    java.util.List<HistoryEventDto> history(UUID studentId);

    FileResourceDto photo(UUID studentId);
}
