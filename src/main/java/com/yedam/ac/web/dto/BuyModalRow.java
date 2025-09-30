package com.yedam.ac.web.dto;

import java.time.LocalDate;

public interface BuyModalRow {
    String getBuyCode();
    String getPartnerName();
    String getProductName();
    Long   getAmountTotal();
    LocalDate getPurchaseDate();
}
