package com.fmud.student.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fmud.storage")
public record StorageProperties(
        String photoPath,
        String documentPath,
        long maxPhotoSizeMb,
        long maxDocumentSizeMb
) {
}
