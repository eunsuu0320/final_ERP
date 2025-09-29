// src/main/java/com/yedam/ac/web/AcPartnerController.java
package com.yedam.ac.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.repository.AcPartnerLookupRepository;
import com.yedam.ac.util.CompanyContext;
import com.yedam.ac.web.dto.PartnerLookupDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AcPartnerController {

    private final AcPartnerLookupRepository repo;
    private final CompanyContext companyCtx;

    @GetMapping("/api/partners")
    public List<PartnerLookupDto> partners(@RequestParam(value = "q", required = false) String q) {
        String cc = companyCtx.getRequiredCompanyCode();
        if (q == null || q.isBlank()) return repo.top200(cc);
        return repo.search(cc, q.trim());
    }
}
