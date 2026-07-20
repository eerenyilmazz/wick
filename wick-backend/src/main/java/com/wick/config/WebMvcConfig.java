package com.wick.config;

import com.wick.photo.config.PhotoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final PhotoProperties photoProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(photoProperties.getPublicUrlPrefix() + "/**")
                .addResourceLocations("file:" + photoProperties.getStoragePath() + "/");
    }
}
