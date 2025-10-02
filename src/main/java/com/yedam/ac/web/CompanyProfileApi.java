// src/main/java/com/yedam/ac/web/CompanyProfileApi.java
package com.yedam.ac.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompanyProfileApi {
    @GetMapping("/api/company/profile")
    public Map<String, Object> profile() {
        return Map.of(
            "name", "(주)구독핑 이알핑",
            "bizNo", "111-22-33333",
            "tel", "1234-5678",
            "ceo", "김예담",
            "address", "대구광역시 중구 중앙대로 403 5층 "
        );
    }
}
