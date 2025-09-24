package com.yedam.ac.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.repository.SalesModalRepository;
import com.yedam.ac.web.dto.SalesModalRow;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SalesModalController {

    private final SalesModalRepository repo;

    // /api/sales/lookup?keyword=...
    @GetMapping("/api/sales/lookup")
    public List<SalesModalRow> lookup(@RequestParam(required = false) String keyword) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return repo.lookup(kw);
    }
}
