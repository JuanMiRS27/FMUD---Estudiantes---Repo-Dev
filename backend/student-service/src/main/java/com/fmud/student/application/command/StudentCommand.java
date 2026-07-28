package com.fmud.student.application.command;

import com.fmud.student.domain.model.StudentStatus;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record StudentCommand(
        String firstName,
        String lastName,
        String documentNumber,
        LocalDate birthDate,
        String birthPlace,
        String address,
        String phone,
        String email,
        StudentStatus status,
        MultipartFile photo
) {
}
