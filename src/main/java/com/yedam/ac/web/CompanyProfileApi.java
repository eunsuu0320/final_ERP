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
            "bizNo", "220-12-34567",
            "tel", "1588-5333",
            "ceo", "김신래",
            "address", "서울특별시 강동구 고덕비즈밸리로6길 84 (고덕동) (주)이카운트"
        );
    }
}
