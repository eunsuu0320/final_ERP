package com.yedam.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CaptchaFilter extends OncePerRequestFilter {

    private static final String SECRET_KEY = "6LdsS9MrAAAAAFxBOrnhIZNFvWLS05AahOl5TfCI";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("/doLogin".equals(request.getServletPath()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String recaptchaResponse = request.getParameter("g-recaptcha-response");

            if (!verifyRecaptcha(recaptchaResponse)) {
                response.sendRedirect("/common/login?captchaError=true");
                return;
            }
        }

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
            conn.getOutputStream().write(params.getBytes());

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


