package com.wick.photo.mapper;

import com.wick.photo.config.PhotoProperties;
import com.wick.photo.domain.UserPhoto;
import com.wick.photo.dto.PhotoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhotoMapper {

    private final PhotoProperties photoProperties;

    public PhotoResponse toResponse(UserPhoto photo) {
        return new PhotoResponse(
                photo.getId(),
                photoProperties.getPublicUrlPrefix() + "/" + photo.getStorageKey(),
                photo.getDisplayOrder()
        );
    }
}
