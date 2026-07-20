package com.wick.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.billing.google-play")
public class GooglePlayBillingProperties {

    private String serviceAccountKeyPath;
    private String packageName;
}
