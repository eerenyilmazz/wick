package com.wick.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.system")
public class SystemProperties {
    private boolean maintenanceMode = false;
    private String minVersion = "0.1.0";
}
