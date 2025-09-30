// src/main/java/com/yedam/ac/web/SalesModalController.java
package com.yedam.ac.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.repository.SalesModalRepository;
import com.yedam.ac.util.CompanyContext;
import com.yedam.ac.web.dto.SalesModalRow;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SalesModalController {

    private final SalesModalRepository repo;
    private final CompanyContext companyCtx;

    @GetMapping("/api/sales/lookup")
    public List<SalesModalRow> lookup(@RequestParam(value="keyword", required=false) String keyword) {
        final String cc = companyCtx.getRequiredCompanyCode();
        final String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return repo.lookup(cc, kw);
    }
}
