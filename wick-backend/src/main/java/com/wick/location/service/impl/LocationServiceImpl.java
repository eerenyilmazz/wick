package com.wick.location.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wick.location.service.LocationService;
import com.wick.user.domain.User;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class LocationServiceImpl implements LocationService {

    private static final String DATASET_PATH = "data/turkiye-il-ilce.json";

    private final ObjectMapper objectMapper;
    private Map<String, List<String>> provinceToDistricts = Map.of();

    public LocationServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadDataset() throws IOException {
        Map<String, List<String>> raw = objectMapper.readValue(
                new ClassPathResource(DATASET_PATH).getInputStream(),
                new TypeReference<Map<String, List<String>>>() { });
        provinceToDistricts = new TreeMap<>(raw);
    }

    @Override
    public List<String> getProvinces() {
        return new ArrayList<>(provinceToDistricts.keySet());
    }

    @Override
    public List<String> getDistricts(String province) {
        List<String> districts = provinceToDistricts.get(province);
        if (districts == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(districts);
    }

    @Override
    public boolean isValidProvince(String province) {
        return provinceToDistricts.containsKey(province);
    }

    @Override
    public boolean isValidDistrict(String province, String district) {
        List<String> districts = provinceToDistricts.get(province);
        return districts != null && districts.contains(district);
    }

    @Override
    public Double calculateDistanceKm(User from, User to) {
        if (from.getLatitude() == null || from.getLongitude() == null
                || to.getLatitude() == null || to.getLongitude() == null) {
            return null;
        }
        double fromLatRad = Math.toRadians(from.getLatitude());
        double toLatRad = Math.toRadians(to.getLatitude());
        double lngDiffRad = Math.toRadians(to.getLongitude() - from.getLongitude());
        double cosCentralAngle = Math.cos(fromLatRad) * Math.cos(toLatRad) * Math.cos(lngDiffRad)
                + Math.sin(fromLatRad) * Math.sin(toLatRad);
        return 6371 * Math.acos(Math.max(-1, Math.min(1, cosCentralAngle)));
    }
}
