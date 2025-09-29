package com.yedam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(new CaptchaFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/common/login", "/common/findPassword", "/css/**", "/js/**", "/erp/**", "/hr/**", "/main/**", "/subscription", "/api/**", "/ac/**", "/sales2/**").permitAll()
                .requestMatchers("/pay/kakao/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/common/login")
                .loginProcessingUrl("/doLogin")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/common/login?error=true")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/common/login?logout=true")
            )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())
            )
        	.csrf(csrf -> csrf
        	    .ignoringRequestMatchers("/api/**", "/pay/**") // REST API만 CSRF 제외
        	);



        return http.build();
    }

}

