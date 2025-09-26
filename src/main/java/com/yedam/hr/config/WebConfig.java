package com.yedam.hr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 사인 이미지 URL 매핑
        registry.addResourceHandler("/hr/sign/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/hr/sign/");

        // PDF URL 매핑
        registry.addResourceHandler("/hr/pdf/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/hr/pdf/");
    }
}