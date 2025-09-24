// com.yedam.ac.web/PartnerController.java
package com.yedam.ac.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.repository.PartnerLookupRepository;
import com.yedam.ac.web.dto.PartnerLookupDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerLookupRepository repo;

    @GetMapping("/api/partners")
    public List<PartnerLookupDto> partners(@RequestParam(value = "q", required = false) String q) {
        if (q == null || q.isBlank()) return repo.top200();
        return repo.search(q.trim());
    }
}
