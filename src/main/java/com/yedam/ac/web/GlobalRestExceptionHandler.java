// com/yedam/ac/web/GlobalRestExceptionHandler.java
package com.yedam.ac.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalRestExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> illegal(IllegalStateException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> any(Exception e){
        return ResponseEntity.internalServerError().body(e.getClass().getSimpleName()+": "+e.getMessage());
    }
}
