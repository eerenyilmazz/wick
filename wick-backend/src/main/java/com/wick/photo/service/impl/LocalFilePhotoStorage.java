package com.wick.photo.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.photo.config.PhotoProperties;
import com.wick.photo.service.PhotoStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalFilePhotoStorage implements PhotoStorageService {

    private static final String FILE_EXTENSION = ".jpg";

    private final PhotoProperties photoProperties;

    @Override
    public String store(byte[] jpegContent) {
        String storageKey = UUID.randomUUID() + FILE_EXTENSION;
        Path targetPath = Path.of(photoProperties.getStoragePath()).resolve(storageKey);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, jpegContent);
        } catch (IOException e) {
            throw new ApiException("Failed to store photo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return storageKey;
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(Path.of(photoProperties.getStoragePath()).resolve(storageKey));
        } catch (IOException e) {
            throw new ApiException("Failed to delete photo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
