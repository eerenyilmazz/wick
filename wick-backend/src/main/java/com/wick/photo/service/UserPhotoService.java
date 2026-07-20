package com.wick.photo.service;

import com.wick.photo.domain.UserPhoto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserPhotoService {

    UserPhoto upload(UUID userId, MultipartFile file);

    List<UserPhoto> getForUser(UUID userId);

    void delete(UUID userId, UUID photoId);

    List<UserPhoto> reorder(UUID userId, List<UUID> orderedPhotoIds);

    void deleteAllForUser(UUID userId);
}
