package com.wick.user.controller;

import com.wick.photo.config.PhotoProperties;
import com.wick.user.dto.PendingVerificationResponse;
import com.wick.user.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/verify")
@RequiredArgsConstructor
public class InternalVerificationController {

    private final VerificationService verificationService;
    private final PhotoProperties photoProperties;

    @Value("${app.internal.secret}")
    private String internalSecret;

    @GetMapping("/pending")
    public List<PendingVerificationResponse> listPending(@RequestHeader("X-Internal-Key") String key) {
        checkSecret(key);
        return verificationService.getPendingVerifications();
    }

    @GetMapping("/{userId}/photo")
    public ResponseEntity<Resource> getPhoto(
            @PathVariable UUID userId,
            @RequestHeader("X-Internal-Key") String key) {
        checkSecret(key);
        Path photoPath = Path.of(photoProperties.getStoragePath()).resolve("verification").resolve(userId + ".jpg");
        if (!photoPath.toFile().exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(new FileSystemResource(photoPath));
    }

    @PostMapping("/{userId}/approve")
    public void approve(
            @PathVariable UUID userId,
            @RequestHeader("X-Internal-Key") String key) {
        checkSecret(key);
        verificationService.approve(userId);
    }

    @PostMapping("/{userId}/reject")
    public void reject(
            @PathVariable UUID userId,
            @RequestHeader("X-Internal-Key") String key) {
        checkSecret(key);
        verificationService.reject(userId);
    }

    private void checkSecret(String key) {
        if (!internalSecret.equals(key)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
