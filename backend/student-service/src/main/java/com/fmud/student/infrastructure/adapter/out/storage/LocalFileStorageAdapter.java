package com.fmud.student.infrastructure.adapter.out.storage;

import com.fmud.student.application.dto.FileResourceDto;
import com.fmud.student.application.port.out.FileStoragePort;
import com.fmud.student.infrastructure.exception.BadRequestException;
import com.fmud.student.infrastructure.configuration.StorageProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalFileStorageAdapter implements FileStoragePort {
    private static final Set<String> PHOTO_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> DOCUMENT_TYPES = Set.of("application/pdf", "image/jpeg", "image/png", "image/webp");
    private final Path photoRoot;
    private final Path documentRoot;
    private final long maxPhotoBytes;
    private final long maxDocumentBytes;

    public LocalFileStorageAdapter(StorageProperties properties) {
        this.photoRoot = Path.of(properties.photoPath()).toAbsolutePath().normalize();
        this.documentRoot = Path.of(properties.documentPath()).toAbsolutePath().normalize();
        this.maxPhotoBytes = properties.maxPhotoSizeMb() * 1024 * 1024;
        this.maxDocumentBytes = properties.maxDocumentSizeMb() * 1024 * 1024;
        createDirectories(photoRoot);
        createDirectories(documentRoot);
    }

    @Override
    public StoredFile storePhoto(MultipartFile file) {
        return store(file, photoRoot, PHOTO_TYPES, maxPhotoBytes, "fotografia");
    }

    @Override
    public StoredFile storeDocument(MultipartFile file) {
        return store(file, documentRoot, DOCUMENT_TYPES, maxDocumentBytes, "documento");
    }

    @Override
    public FileResourceDto loadPhoto(String storageKey, String contentType) {
        Path file = resolve(photoRoot, storageKey);
        return new FileResourceDto(new FileSystemResource(file), contentType, "student-photo", size(file));
    }

    @Override
    public FileResourceDto loadDocument(String storageKey, String contentType, String filename) {
        Path file = resolve(documentRoot, storageKey);
        return new FileResourceDto(new FileSystemResource(file), contentType, filename, size(file));
    }

    private StoredFile store(MultipartFile file, Path root, Set<String> allowedTypes, long maxBytes, String label) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Debe adjuntar un archivo.");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!allowedTypes.contains(contentType)) {
            throw new BadRequestException("Formato de " + label + " no permitido.");
        }
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("El archivo supera el tamano maximo permitido.");
        }
        String extension = extensionFor(contentType);
        String key = UUID.randomUUID() + extension;
        Path target = resolve(root, key);
        try {
            file.transferTo(target);
            return new StoredFile(key, contentType, file.getSize());
        } catch (IOException ex) {
            throw new BadRequestException("No fue posible almacenar el archivo.");
        }
    }

    private Path resolve(Path root, String key) {
        if (key == null || key.contains("..") || key.contains("/") || key.contains("\\")) {
            throw new BadRequestException("Ruta de archivo invalida.");
        }
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new BadRequestException("Ruta de archivo invalida.");
        }
        return target;
    }

    private long size(Path file) {
        try {
            return Files.size(file);
        } catch (IOException ex) {
            throw new BadRequestException("No fue posible leer el archivo.");
        }
    }

    private void createDirectories(Path root) {
        try {
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible crear el almacenamiento local", ex);
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "application/pdf" -> ".pdf";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
