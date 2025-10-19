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
        
        
        // sales1 상품 이미지 URL 매핑
        registry.addResourceHandler("/uploads/sales1/productImg/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/sales1/productImg/")
                .setCachePeriod(0); // 캐시 비활성화 (이미지 업로드 후 바로 표시되도록)
    }
}