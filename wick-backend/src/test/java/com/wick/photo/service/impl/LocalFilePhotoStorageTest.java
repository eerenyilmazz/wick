package com.wick.photo.service.impl;

import com.wick.photo.config.PhotoProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFilePhotoStorageTest {

    @Test
    void store_writesJpegBytesToDiskWithRandomName_thenDeleteRemovesIt(@TempDir Path tempDir) throws IOException {
        PhotoProperties photoProperties = new PhotoProperties();
        photoProperties.setStoragePath(tempDir.toString());
        LocalFilePhotoStorage storage = new LocalFilePhotoStorage(photoProperties);

        String storageKey = storage.store(new byte[]{1, 2, 3});

        assertThat(storageKey).endsWith(".jpg");
        Path storedFile = tempDir.resolve(storageKey);
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.readAllBytes(storedFile)).containsExactly(1, 2, 3);

        storage.delete(storageKey);
        assertThat(Files.exists(storedFile)).isFalse();
    }
}
