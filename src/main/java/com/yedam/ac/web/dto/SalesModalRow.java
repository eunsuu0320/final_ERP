package com.yedam.ac.web.dto;

import java.time.LocalDate;

public interface SalesModalRow {
    String getSalesCode();
    String getPartnerName();
    String getProductName();
    Long   getSalesAmount();
    LocalDate getSalesDate();
}
