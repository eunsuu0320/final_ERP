// src/main/java/com/yedam/ac/web/BuyModalController.java
package com.yedam.ac.web;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.repository.BuyLookupRepository;
import com.yedam.ac.util.CompanyContext;
import com.yedam.ac.web.dto.BuyModalRow;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BuyModalController {

    private final BuyLookupRepository repo;
    private final CompanyContext companyCtx;

    @GetMapping("/api/buys/lookup")
    public List<BuyModalRow> lookup(@RequestParam(value="keyword", required=false) String keyword) {
        final String cc = companyCtx.getRequiredCompanyCode();
        final String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return repo.lookup(cc, kw);
    }
}
