package com.fmud.student.application.dto;

import org.springframework.core.io.Resource;

public record FileResourceDto(
        Resource resource,
        String contentType,
        String filename,
        long size
) {
}
