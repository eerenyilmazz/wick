package com.wick.photo.service;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoProcessor {

    byte[] process(MultipartFile file);
}
