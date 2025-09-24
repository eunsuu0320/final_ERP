// com/yedam/ac/util/VoucherNoGenerator.java
package com.yedam.ac.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class VoucherNoGenerator {

    private static final DateTimeFormatter YYMM = DateTimeFormatter.ofPattern("yyMM");

    public static String monthPrefix(LocalDate date) {
        return date.format(YYMM) + "-"; // 예: 2509-
    }

    public static String nextFromMax(String maxLike) {
        // maxLike 예: "2509-0007" 또는 null
        int next = 1;
        if (maxLike != null && maxLike.contains("-")) {
            String seq = maxLike.substring(maxLike.indexOf('-') + 1);
            try { next = Integer.parseInt(seq) + 1; } catch (Exception ignore) { next = 1; }
        }
        return String.format("%04d", next);
    }
}
