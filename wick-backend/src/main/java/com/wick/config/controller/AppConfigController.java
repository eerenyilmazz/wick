package com.wick.config.controller;

import com.wick.config.dto.AppConfigResponse;
import com.wick.config.service.AppConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class AppConfigController {

    private final AppConfigService appConfigService;

    @GetMapping
    public AppConfigResponse getConfig() {
        return appConfigService.getConfig();
    }
}
