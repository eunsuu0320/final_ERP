// src/main/java/com/yedam/ac/web/SupplierController.java
package com.yedam.ac.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.repository.SupplierLookupRepository;
import com.yedam.ac.util.CompanyContext;
import com.yedam.ac.web.dto.SupplierRow;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierLookupRepository repo;
    private final CompanyContext companyCtx;

    @GetMapping("/api/suppliers")
    public List<SupplierRow> list(@RequestParam(required = false, name = "kw") String kw){
        String cc = companyCtx.getRequiredCompanyCode();
        return repo.searchSuppliers(cc, kw == null ? "" : kw.trim());
    }
}
