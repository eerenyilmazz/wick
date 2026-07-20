package com.wick.user.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.notification.service.NotificationService;
import com.wick.photo.config.PhotoProperties;
import com.wick.user.domain.User;
import com.wick.user.domain.VerificationStatus;
import com.wick.user.dto.PendingVerificationResponse;
import com.wick.user.repository.UserRepository;
import com.wick.user.service.UserService;
import com.wick.user.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PhotoProperties photoProperties;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void submitVerification(UUID userId, MultipartFile photo) {
        User user = userService.getById(userId);
        if (user.getVerificationStatus() == VerificationStatus.VERIFIED ||
                user.getVerificationStatus() == VerificationStatus.PENDING) {
            return;
        }
        user.setVerificationStatus(VerificationStatus.PENDING);
        user.setVerificationSubmittedAt(java.time.Instant.now());
        userRepository.save(user);
        storeVerificationPhoto(userId, photo);
    }

    @Override
    @Transactional
    public void approve(UUID userId) {
        User user = userService.getById(userId);
        user.setVerified(true);
        user.setVerificationStatus(VerificationStatus.VERIFIED);
        userRepository.save(user);
        notificationService.notifyVerificationApproved(user);
    }

    @Override
    @Transactional
    public void reject(UUID userId) {
        User user = userService.getById(userId);
        user.setVerificationStatus(VerificationStatus.REJECTED);
        userRepository.save(user);
        notificationService.notifyVerificationRejected(user);
    }

    @Override
    public List<PendingVerificationResponse> getPendingVerifications() {
        return userRepository.findAllByVerificationStatus(VerificationStatus.PENDING).stream()
                .map(u -> new PendingVerificationResponse(
                        u.getId(),
                        u.getDisplayName(),
                        u.getEmail(),
                        u.getVerificationSubmittedAt(),
                        "/internal/verify/" + u.getId() + "/photo"))
                .toList();
    }

    private void storeVerificationPhoto(UUID userId, MultipartFile photo) {
        Path verificationDir = Path.of(photoProperties.getStoragePath()).resolve("verification");
        try {
            Files.createDirectories(verificationDir);
            Path target = verificationDir.resolve(userId + ".jpg");
            Files.write(target, photo.getBytes());
        } catch (IOException e) {
            throw new ApiException("Failed to store verification photo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
