package com.yedam.ac.web.dto;

import java.time.LocalDate;
import lombok.Data;

/**
 * 전표 등록 요청 DTO (매출/매입 공통)
 */
@Data
public class StatementCreateRequest {
    // 필수
    private String type;            // "SALES" | "BUY"
    private LocalDate voucherDate;  // 전표일자
    private String voucherNo;       // 화면에서 생성한 yyyy-#### (없어도 됨)

    // 선택/부가
    private String saleCode;
    private String partnerName;
    private LocalDate saleDate;
    private String empName;
    private LocalDate writeDate;

    private String taxType;   // "TAXABLE" | "ZERO" | "EXEMPT"
    private Long supply;      // 공급가액
    private Long vat;         // 부가세
    private Long total;       // 총액
    private String remark;
}
