// src/main/java/com/yedam/config/SecurityConfig.java
package com.yedam.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception { return cfg.getAuthenticationManager(); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .addFilterBefore(new CaptchaFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/", "/common/**", "/css/**", "/js/**",
                               "/erp/**", "/hr/**", "/main/**",
                               "/subscription",
                               "/api/**", "/ac/**", "/sales2/**",
                               "/pay/**", "/pay/kakao/**", "/pay/naver/**", "/pay/toss/**",
                               "/success", "/error",
                               "/forbidden" // forbidden 화면은 공개
              ).permitAll()
              .requestMatchers("/statements").authenticated()
              .anyRequest().authenticated()
          )
          .formLogin(f -> f
              .loginPage("/common/login")
              .loginProcessingUrl("/doLogin")
              .defaultSuccessUrl("/dashboard", true)
              .failureUrl("/common/login?error=true")
          )
          .logout(l -> l
              .logoutUrl("/logout")
              .logoutSuccessUrl("/common/login?logout=true")
          )
          .headers(h -> h.frameOptions(fr -> fr.sameOrigin()))
          .csrf(c -> c.ignoringRequestMatchers("/api/**", "/pay/**"))
          .exceptionHandling(ex -> ex
              // 401 처리
              .authenticationEntryPoint((req, res, e) -> {
                  if (isApi(req)) {
                      res.setStatus(401);
                      res.setContentType("text/plain;charset=UTF-8");
                      res.getWriter().write("로그인이 필요합니다.");
                  } else {
                      res.sendRedirect("/common/login");
                  }
              })
          );

        return http.build();
    }

    private boolean isApi(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        String xhr    = req.getHeader("X-Requested-With");
        String ct     = req.getHeader("Content-Type");
        return (accept != null && accept.contains("application/json"))
            || "XMLHttpRequest".equalsIgnoreCase(xhr)
            || (ct != null && ct.contains("application/json"))
            || req.getRequestURI().startsWith("/api/");
    }
}
