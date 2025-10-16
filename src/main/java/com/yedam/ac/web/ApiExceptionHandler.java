//package com.yedam.ac.web;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import jakarta.servlet.http.HttpServletRequest;
//
//@RestControllerAdvice
//public class ApiExceptionHandler {
//
//    @ExceptionHandler(IllegalStateException.class)
//    public ResponseEntity<Map<String, Object>> onIllegalState(IllegalStateException e, HttpServletRequest req) {
//        Map<String, Object> body = new LinkedHashMap<>();
//        body.put("path", req.getRequestURI());
//        body.put("message", e.getMessage());
//        // 회사코드 누락은 400으로 내려서 프론트가 바로 원인 알 수 있게
//        if (e.getMessage() != null && e.getMessage().contains("회사코드")) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
//        }
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, Object>> onAny(Exception e, HttpServletRequest req) {
//        Map<String, Object> body = new LinkedHashMap<>();
//        body.put("path", req.getRequestURI());
//        body.put("message", e.getMessage());
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
//    }
//}
