package com.yedam.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
public class CaptchaFilter extends OncePerRequestFilter {

//    @Value("${recaptcha.secret-key}")
    private String SECRET_KEY = "6Let1_MrAAAAAE1JqSXrMYjNBurvrg5_6pjxRw7f";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        System.out.println(SECRET_KEY+"=====================");
        // 로그인 요청일 때만 캡차 검증
        if ("/doLogin".equals(path) && "POST".equalsIgnoreCase(request.getMethod())) {
            String recaptchaResponse = request.getParameter("g-recaptcha-response");

            // 캡차 응답이 없으면 실패 처리
            if (recaptchaResponse == null || recaptchaResponse.isBlank() || !verifyRecaptcha(recaptchaResponse)) {
            	System.out.println(recaptchaResponse+"***********************");
                response.sendRedirect("/common/login?captchaError=true");
                return;
            }
        }

        // 나머지 요청은 그냥 통과
        filterChain.doFilter(request, response);
    }

    private boolean verifyRecaptcha(String recaptchaResponse) {
        try {
            String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";
            String params = "secret=" + SECRET_KEY + "&response=" + recaptchaResponse;

            URL url = new URL(verifyUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes());
                os.flush();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            System.out.println("Google reCAPTCHA API response: " + content);

            return content.toString().contains("\"success\": true");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
