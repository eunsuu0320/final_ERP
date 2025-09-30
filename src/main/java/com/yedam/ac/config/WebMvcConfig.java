package com.yedam.ac.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final HibernateFilterEnabler hibernateFilterEnabler;
    private final CompanyCodeTraceInterceptor ccTrace;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(hibernateFilterEnabler).addPathPatterns("/**");
        registry.addInterceptor(ccTrace).addPathPatterns("/**");
    }
}
