package com.yedam.ac.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> onAny(Exception e, HttpServletRequest req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("path", req.getRequestURI());
        body.put("message", e.getMessage());
        return ResponseEntity.status(500).body(body);
    }
}