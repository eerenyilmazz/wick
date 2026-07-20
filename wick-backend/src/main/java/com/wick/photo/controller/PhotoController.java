package com.wick.photo.controller;

import com.wick.photo.dto.PhotoResponse;
import com.wick.photo.dto.ReorderPhotosRequest;
import com.wick.photo.mapper.PhotoMapper;
import com.wick.photo.service.UserPhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final UserPhotoService userPhotoService;
    private final PhotoMapper photoMapper;

    @PostMapping
    public PhotoResponse upload(@AuthenticationPrincipal String userId, @RequestParam("file") MultipartFile file) {
        return photoMapper.toResponse(userPhotoService.upload(UUID.fromString(userId), file));
    }

    @GetMapping
    public List<PhotoResponse> getPhotos(@AuthenticationPrincipal String userId) {
        return userPhotoService.getForUser(UUID.fromString(userId)).stream()
                .map(photoMapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{photoId}")
    public void delete(@AuthenticationPrincipal String userId, @PathVariable UUID photoId) {
        userPhotoService.delete(UUID.fromString(userId), photoId);
    }

    @PutMapping("/order")
    public List<PhotoResponse> reorder(@AuthenticationPrincipal String userId,
                                        @Valid @RequestBody ReorderPhotosRequest request) {
        return userPhotoService.reorder(UUID.fromString(userId), request.photoIds()).stream()
                .map(photoMapper::toResponse)
                .toList();
    }
}
