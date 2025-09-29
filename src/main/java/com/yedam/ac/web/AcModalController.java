// src/main/java/com/yedam/ac/web/AcModalController.java
package com.yedam.ac.web;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.domain.AcPartner;
import com.yedam.ac.repository.AcPartnerRepository;
import com.yedam.ac.repository.AcSalesRepository;
import com.yedam.ac.util.CompanyContext;
import com.yedam.sales2.domain.Sales;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/modal")
public class AcModalController {

    private final AcPartnerRepository acPartnerRepository;
    private final AcSalesRepository   acSalesRepository;
    private final CompanyContext      companyCtx;

    @GetMapping("/partners")
    public List<AcPartner> partners(@RequestParam(required = false, defaultValue = "") String q) {
        String cc = companyCtx.getRequiredCompanyCode();
        var top200 = PageRequest.of(0, 200);

        if (q == null || q.isBlank()) {
            return acPartnerRepository.findTopByCompanyOrderByName(cc, top200);
        }
        return acPartnerRepository.searchTopByCompanyAndKeyword(cc, q, top200);
    }

    @GetMapping("/sales")
    public List<Sales> sales(@RequestParam(required = false, defaultValue = "") String q) {
        String cc = companyCtx.getRequiredCompanyCode();
        return acSalesRepository
                .findTop50ByCompanyCodeAndSaleCodeContainingIgnoreCaseOrderBySalesDateDesc(cc, q);
    }
}
