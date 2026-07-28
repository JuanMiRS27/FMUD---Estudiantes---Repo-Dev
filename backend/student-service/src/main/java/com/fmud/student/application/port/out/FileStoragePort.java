package com.fmud.student.application.port.out;

import com.fmud.student.application.dto.FileResourceDto;
import org.springframework.web.multipart.MultipartFile;

public interface FileStoragePort {
    StoredFile storePhoto(MultipartFile file);

    StoredFile storeDocument(MultipartFile file);

    FileResourceDto loadPhoto(String storageKey, String contentType);

    FileResourceDto loadDocument(String storageKey, String contentType, String filename);

    record StoredFile(String storageKey, String contentType, long size) {
    }
}
