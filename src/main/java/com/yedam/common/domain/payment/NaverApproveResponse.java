package com.yedam.common.domain.payment;

import lombok.Data;

@Data
public class NaverApproveResponse {
    private String resultCode;     // 결과 코드 (Ex: "Success")
    private String resultMessage;  // 결과 메시지
    private String paymentId;      // 결제 ID
    private int totalPayAmount;    // 결제 금액
    private String merchantPayKey; // 가맹점에서 전달한 주문번호
    private String payMethod;      // 결제수단 (CARD, POINT 등)
}
