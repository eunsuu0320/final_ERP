// src/main/java/com/yedam/ac/web/IncomeStatementController.java
package com.yedam.ac.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.service.IncomeStatementService;
import com.yedam.ac.web.dto.IncomeStatementDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/income")
@RequiredArgsConstructor
public class IncomeStatementController {
  private final IncomeStatementService svc;

  // /api/income?from=2025-01-01&to=2025-12-31
  @GetMapping
  public IncomeStatementDto get(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return svc.get(from, to);
  }
}
