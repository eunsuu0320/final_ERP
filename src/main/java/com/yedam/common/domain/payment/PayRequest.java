package com.yedam.common.domain.payment;

import lombok.Data;

@Data
public class PayRequest {
    private String orderId;
    private String userId;
    private String itemName;
    private int amount;
    private String payMethod; // "KAKAO" or "NAVER" 구분
    private String buyerName;
    
    // 구독(회사) 정보
    private String companyName;   // 회사명
    private String ceoName;       // 대표자명
    private String bizRegNo;      // 사업자 등록번호
    private String address;       // 주소
    private String addressDetail; // 상세 주소
    private String tel;           // 담당자 연락처
}

