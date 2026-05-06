package com.ucto.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration to register interceptors for request logging and monitoring.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SentryLoggingInterceptor sentryLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sentryLoggingInterceptor)
                .addPathPatterns("/api/**");
    }
}
