package com.lazydrop.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppConfig {

    @Value("${app.join.base.url}")
    private String joinUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.cookies.secure}")
    private boolean secureCookies;
}
