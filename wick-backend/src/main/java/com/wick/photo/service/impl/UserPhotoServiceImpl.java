package com.wick.photo.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.common.exception.ResourceNotFoundException;
import com.wick.photo.config.PhotoProperties;
import com.wick.photo.domain.UserPhoto;
import com.wick.photo.repository.UserPhotoRepository;
import com.wick.photo.service.PhotoProcessor;
import com.wick.photo.service.PhotoStorageService;
import com.wick.photo.service.PhotoUploadRateLimiter;
import com.wick.photo.service.UserPhotoService;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPhotoServiceImpl implements UserPhotoService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final UserPhotoRepository userPhotoRepository;
    private final PhotoStorageService photoStorageService;
    private final PhotoProcessor photoProcessor;
    private final PhotoProperties photoProperties;
    private final UserService userService;
    private final PhotoUploadRateLimiter photoUploadRateLimiter;

    @Override
    @Transactional
    public UserPhoto upload(UUID userId, MultipartFile file) {
        if (file.isEmpty() || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ApiException("Photo must be a JPEG or PNG image", HttpStatus.BAD_REQUEST);
        }
        validateMagicBytes(file);
        photoUploadRateLimiter.consumeOrThrow(userId);
        int currentCount = userPhotoRepository.countByUserId(userId);
        if (currentCount >= photoProperties.getMaxPhotosPerUser()) {
            throw new ApiException("Maximum number of photos reached", HttpStatus.CONFLICT);
        }

        User user = userService.getById(userId);
        byte[] processed = photoProcessor.process(file);
        String storageKey = photoStorageService.store(processed);
        UserPhoto photo = new UserPhoto();
        photo.setUser(user);
        photo.setStorageKey(storageKey);
        photo.setDisplayOrder(currentCount);
        return userPhotoRepository.save(photo);
    }

    @Override
    public List<UserPhoto> getForUser(UUID userId) {
        return userPhotoRepository.findAllByUserIdOrderByDisplayOrderAsc(userId);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID photoId) {
        UserPhoto photo = getOwnedPhoto(userId, photoId);
        photoStorageService.delete(photo.getStorageKey());
        userPhotoRepository.delete(photo);

        List<UserPhoto> remaining = userPhotoRepository.findAllByUserIdOrderByDisplayOrderAsc(userId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setDisplayOrder(i);
        }
        userPhotoRepository.saveAll(remaining);
    }

    @Override
    @Transactional
    public List<UserPhoto> reorder(UUID userId, List<UUID> orderedPhotoIds) {
        List<UserPhoto> existing = userPhotoRepository.findAllByUserIdOrderByDisplayOrderAsc(userId);
        Set<UUID> existingIds = existing.stream().map(UserPhoto::getId).collect(Collectors.toSet());
        if (!existingIds.equals(Set.copyOf(orderedPhotoIds)) || existingIds.size() != orderedPhotoIds.size()) {
            throw new ApiException("Photo order must include exactly the user's existing photos", HttpStatus.BAD_REQUEST);
        }

        for (int i = 0; i < orderedPhotoIds.size(); i++) {
            UUID photoId = orderedPhotoIds.get(i);
            UserPhoto photo = existing.stream()
                    .filter(p -> p.getId().equals(photoId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));
            photo.setDisplayOrder(i);
        }
        return userPhotoRepository.saveAll(existing).stream()
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .toList();
    }

    @Override
    @Transactional
    public void deleteAllForUser(UUID userId) {
        List<UserPhoto> photos = userPhotoRepository.findAllByUserIdOrderByDisplayOrderAsc(userId);
        for (UserPhoto photo : photos) {
            photoStorageService.delete(photo.getStorageKey());
        }
        userPhotoRepository.deleteAll(photos);
    }

    private void validateMagicBytes(MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            if (content.length >= 3
                    && (content[0] & 0xFF) == 0xFF
                    && (content[1] & 0xFF) == 0xD8
                    && (content[2] & 0xFF) == 0xFF) {
                return;
            }
            if (content.length >= 8
                    && (content[0] & 0xFF) == 0x89
                    && (content[1] & 0xFF) == 0x50
                    && (content[2] & 0xFF) == 0x4E
                    && (content[3] & 0xFF) == 0x47
                    && (content[4] & 0xFF) == 0x0D
                    && (content[5] & 0xFF) == 0x0A
                    && (content[6] & 0xFF) == 0x1A
                    && (content[7] & 0xFF) == 0x0A) {
                return;
            }
            throw new ApiException("Invalid file type", HttpStatus.BAD_REQUEST);
        } catch (java.io.IOException e) {
            throw new ApiException("Invalid file type", HttpStatus.BAD_REQUEST);
        }
    }

    private UserPhoto getOwnedPhoto(UUID userId, UUID photoId) {
        UserPhoto photo = userPhotoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));
        if (!photo.getUser().getId().equals(userId)) {
            throw new ApiException("You do not own this photo", HttpStatus.FORBIDDEN);
        }
        return photo;
    }
}
