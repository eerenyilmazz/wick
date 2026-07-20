package com.wick.match.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.match")
public class MatchProperties {

    private int dailySwipeLimit;
    private int swipeLimitWindowHours;
}
