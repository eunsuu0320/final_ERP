// src/main/java/com/yedam/ac/service/VoucherNoService.java
package com.yedam.ac.service;

import java.time.LocalDate;

public interface VoucherNoService {
    String next(String type, LocalDate date, String companyCode);
}
