package com.wick.user.service;

import com.wick.user.dto.DataExportResponse;

import java.util.UUID;

public interface DataExportService {

    DataExportResponse export(UUID userId);
}
