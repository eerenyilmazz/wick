package com.wick.payment.config;

import com.wick.payment.domain.ProductType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.billing")
public class BillingProperties {

    private Map<String, ProductConfig> products = Map.of();

    @Getter
    @Setter
    public static class ProductConfig {

        private ProductType type;
        private int amount;
    }
}
