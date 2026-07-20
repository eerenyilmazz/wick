package com.wick.photo.repository;

import com.wick.photo.domain.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserPhotoRepository extends JpaRepository<UserPhoto, UUID> {

    List<UserPhoto> findAllByUserIdOrderByDisplayOrderAsc(UUID userId);

    int countByUserId(UUID userId);
}
