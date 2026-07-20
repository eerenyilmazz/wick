package com.wick.session.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.session")
public class SessionProperties {

    private int freeDurationMinutes;
    private int premiumDurationMinutes;
    private int extensionMinutes;
}
