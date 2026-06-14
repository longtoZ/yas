package com.yas.tax;

import com.yas.commonlibrary.config.CorsConfig;
import com.yas.tax.config.ServiceUrlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"com.yas.tax", "com.yas.commonlibrary"})
@EnableConfigurationProperties({ServiceUrlConfig.class, CorsConfig.class})
public class TaxApplication {

    private static final Logger log = LoggerFactory.getLogger(TaxApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TaxApplication.class, args);
        log.info(">>> YAS tax service started from branch dev_tax_service (developer_build test marker) <<<");
    }
}
