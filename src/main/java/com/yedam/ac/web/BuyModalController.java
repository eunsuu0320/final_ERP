package com.yedam.ac.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.domain.BuyListView;
import com.yedam.ac.repository.BuyLookupRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BuyModalController {

    private final BuyLookupRepository repo;

    @GetMapping("/api/buys/lookup")
    public List<BuyListView> lookup(@RequestParam(required = false, name = "kw") String kw) {
        return repo.search(kw == null ? "" : kw.trim());
    }
}
