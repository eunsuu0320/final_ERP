// src/main/java/com/yedam/ac/config/WebMvcConfig.java
package com.yedam.ac.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final HibernateFilterEnabler hibernateFilterEnabler;
    private final CompanyCodeTraceInterceptor ccTrace; // ★ 추가

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(hibernateFilterEnabler).addPathPatterns("/**");
        registry.addInterceptor(ccTrace).addPathPatterns("/**"); // ★ 뒤에 붙여도 되고, 앞에 붙여도 됨
    }
}
