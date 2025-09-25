// src/main/java/com/yedam/ac/service/VoucherNoService.java
package com.yedam.ac.service;

import java.time.LocalDate;

public interface VoucherNoService {
    /**
     * @param type  "SALES" | "BUY"
     * @param date  null이면 today. 포맷은 yyMM-#### (월별 증가)
     * @return      다음 전표번호
     */
    String next(String type, LocalDate date);
    
    String next1(String type, LocalDate date);
}
