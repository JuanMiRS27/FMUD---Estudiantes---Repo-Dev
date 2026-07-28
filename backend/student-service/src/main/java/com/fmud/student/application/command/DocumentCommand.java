package com.fmud.student.application.command;

import org.springframework.web.multipart.MultipartFile;

public record DocumentCommand(
        String documentType,
        String displayName,
        String description,
        MultipartFile file
) {
}
