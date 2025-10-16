// src/main/java/com/yedam/dash/web/DashboardController.java
package com.yedam.dash.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.dash.dto.DashboardSummaryDto;
import com.yedam.dash.security.CompanyResolver;
import com.yedam.dash.service.DashboardService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CompanyResolver companyResolver;

    @GetMapping("/api/dashboard/summary")
    public ResponseEntity<DashboardSummaryDto> summary(HttpServletRequest request){
        String companyCode = companyResolver.resolveCompanyCode(request);
        return ResponseEntity.ok(dashboardService.load(companyCode));
    }
}
