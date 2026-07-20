package com.wick.location.service;

import com.wick.user.domain.User;

import java.util.List;

public interface LocationService {

    List<String> getProvinces();

    List<String> getDistricts(String province);

    boolean isValidProvince(String province);

    boolean isValidDistrict(String province, String district);

    Double calculateDistanceKm(User from, User to);
}
