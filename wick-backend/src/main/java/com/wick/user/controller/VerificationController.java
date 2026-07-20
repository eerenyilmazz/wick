package com.wick.user.controller;

import com.wick.user.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile/verify")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping(consumes = "multipart/form-data")
    public void submitVerification(
            @AuthenticationPrincipal String userId,
            @RequestPart("photo") MultipartFile photo) {
        verificationService.submitVerification(UUID.fromString(userId), photo);
    }
}
