// src/main/java/com/yedam/ac/repository/VoucherNoRepository.java
package com.yedam.ac.repository;

import java.time.LocalDate;

public interface VoucherNoRepository {
    String next(String companyCode, String kind, LocalDate baseDate);
}
