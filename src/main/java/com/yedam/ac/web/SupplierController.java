package com.yedam.ac.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.repository.SupplierLookupRepository;
import com.yedam.ac.web.dto.SupplierRow;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierLookupRepository repo;

    @GetMapping("/api/suppliers")
    public List<SupplierRow> list(@RequestParam(required = false, name = "kw") String kw){
        return repo.searchSuppliers(kw == null ? "" : kw.trim());
    }
}
