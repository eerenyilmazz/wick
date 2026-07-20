package com.wick.photo.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.photo.config.PhotoProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThumbnailatorPhotoProcessorTest {

    private ThumbnailatorPhotoProcessor processor;

    @BeforeEach
    void setUp() {
        PhotoProperties photoProperties = new PhotoProperties();
        photoProperties.setMaxDimensionPx(1080);
        photoProperties.setJpegQuality(0.85);
        processor = new ThumbnailatorPhotoProcessor(photoProperties);
    }

    private byte[] jpegBytesOf(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return output.toByteArray();
    }

    @Test
    void process_shrinksLargeImageToFitWithinMaxDimension() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "big.jpg", "image/jpeg", jpegBytesOf(2000, 1500));

        byte[] result = processor.process(file);

        BufferedImage resized = ImageIO.read(new java.io.ByteArrayInputStream(result));
        assertThat(resized.getWidth()).isLessThanOrEqualTo(1080);
        assertThat(resized.getHeight()).isLessThanOrEqualTo(1080);
    }

    @Test
    void process_throwsBadRequest_whenImageIsCorrupt() {
        MockMultipartFile file = new MockMultipartFile("file", "corrupt.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> processor.process(file)).isInstanceOf(ApiException.class);
    }
}
