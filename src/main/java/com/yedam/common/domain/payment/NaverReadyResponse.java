package com.yedam.common.domain.payment;

import lombok.Data;

@Data
public class NaverReadyResponse {
    private String paymentId;   // 결제 ID (네이버페이에서 발급)
    private String confirmUrl;  // 결제 승인 URL
    private String cancelUrl;   // 취소 URL
    private String failUrl;     // 실패 URL
}
