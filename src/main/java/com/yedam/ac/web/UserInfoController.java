package com.yedam.ac.web;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserInfoController {

    @GetMapping("/api/me")
    public ResponseEntity<?> me(Principal principal) {
        String name = principal != null ? principal.getName() : "";
        return ResponseEntity.ok(new Me(name));
    }

    record Me(String name) {}
}
