package com.wick.session.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ContactInfoDetector {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?\\d[\\d\\s().-]{6,}\\d)");
    private static final Pattern HANDLE_PATTERN =
            Pattern.compile("(?i)(instagram\\.com/\\S+|whatsapp\\.com/\\S+|wa\\.me/\\S+|@[a-zA-Z0-9._]{3,30})");

    public boolean containsContactInfo(String content) {
        return PHONE_PATTERN.matcher(content).find() || HANDLE_PATTERN.matcher(content).find();
    }
}
