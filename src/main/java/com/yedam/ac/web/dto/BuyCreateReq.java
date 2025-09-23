package com.yedam.ac.web.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BuyCreateReq {
    private LocalDate voucherDate;
    private String buyCode;
    private String partnerName;
    private String employee;
    private String taxCode;
    private Long amountSupply;
    private Long amountVat;
    private Long amountTotal;
    private String remark;
}