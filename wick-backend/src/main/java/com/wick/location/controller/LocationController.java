package com.wick.location.controller;

import com.wick.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/provinces")
    public List<String> getProvinces() {
        return locationService.getProvinces();
    }

    @GetMapping("/provinces/{province}/districts")
    public List<String> getDistricts(@PathVariable String province) {
        return locationService.getDistricts(province);
    }
}
