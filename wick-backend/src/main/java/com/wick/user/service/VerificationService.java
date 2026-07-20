package com.wick.user.service;

import com.wick.user.dto.PendingVerificationResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface VerificationService {

    void submitVerification(UUID userId, MultipartFile photo);

    void approve(UUID userId);

    void reject(UUID userId);

    List<PendingVerificationResponse> getPendingVerifications();
}
