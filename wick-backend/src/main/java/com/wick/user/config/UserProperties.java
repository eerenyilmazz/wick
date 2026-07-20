package com.wick.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.user")
public class UserProperties {

    private int minimumAge;
    private int maximumAge;
}
