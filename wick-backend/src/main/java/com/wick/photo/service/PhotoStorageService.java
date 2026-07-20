package com.wick.photo.service;

public interface PhotoStorageService {

    String store(byte[] jpegContent);

    void delete(String storageKey);
}
