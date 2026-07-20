package com.wick.session.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ContactInfoDetectorTest {

    private final ContactInfoDetector detector = new ContactInfoDetector();

    @ParameterizedTest
    @ValueSource(strings = {
            "call me at 0532 123 45 67",
            "+90 532 123 45 67 whatsapp me",
            "follow me @eren.codes",
            "instagram.com/eren",
            "wa.me/905321234567"
    })
    void containsContactInfo_returnsTrue_forKnownPatterns(String content) {
        assertThat(detector.containsContactInfo(content)).isTrue();
    }

    @Test
    void containsContactInfo_returnsFalse_forOrdinaryMessage() {
        assertThat(detector.containsContactInfo("Hey, how was your day?")).isFalse();
    }
}
