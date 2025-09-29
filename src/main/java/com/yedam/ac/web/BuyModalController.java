// src/main/java/com/yedam/ac/web/BuyModalController.java
package com.yedam.ac.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.domain.BuyListView;
import com.yedam.ac.repository.BuyLookupRepository;
import com.yedam.ac.util.CompanyContext;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BuyModalController {

    private final BuyLookupRepository repo;
    private final CompanyContext companyCtx;

    @GetMapping("/api/buys/lookup")
    public List<BuyListView> lookup(@RequestParam(required = false, name = "kw") String kw) {
        String cc = companyCtx.getRequiredCompanyCode();
        return repo.search(cc, (kw == null) ? "" : kw.trim());
    }
}
