package com.yedam.ac.web.dto;

import java.time.LocalDate;

public interface SalesModalRow {
    String getSalesCode();      // SALES_CODE
    String getCorrespondent();  // CORRESPONDENT
    String getProductName();    // PRODUCT_NAME
    Long   getSalesAmount();    // SALES_AMOUNT
    LocalDate getSalesDate();   // SALES_DATE
}
