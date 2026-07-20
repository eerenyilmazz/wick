package com.wick.photo.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.photo.config.PhotoProperties;
import com.wick.photo.service.PhotoProcessor;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ThumbnailatorPhotoProcessor implements PhotoProcessor {

    private final PhotoProperties photoProperties;

    @Override
    public byte[] process(MultipartFile file) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(photoProperties.getMaxDimensionPx(), photoProperties.getMaxDimensionPx())
                    .outputFormat("jpg")
                    .outputQuality(photoProperties.getJpegQuality())
                    .toOutputStream(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new ApiException("Could not process photo: unsupported or corrupt image", HttpStatus.BAD_REQUEST);
        }
    }
}
