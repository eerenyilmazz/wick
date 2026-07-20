package com.wick.photo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.photo")
public class PhotoProperties {

    private String storagePath;
    private int maxPhotosPerUser;
    private String publicUrlPrefix;
    private int maxDimensionPx;
    private double jpegQuality;
    private int maxUploadsPerDay;
    private int uploadLimitWindowHours;
}
