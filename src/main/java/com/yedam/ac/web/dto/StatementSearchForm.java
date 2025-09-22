package com.yedam.ac.web.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class StatementSearchForm {
    private String type = "ALL";          // ALL/SALES/BUY/MONEY/PAYMENT
    private String keyword;               // 거래처명 or 전표번호
    private String voucherNo;             // 전표번호
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    private Integer page = 0;             // 0-based
    private Integer size = 10;
}
