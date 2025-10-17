package com.yedam.sales1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 모달 검색 결과(거래처)를 위한 DTO
 * 불필요한 연관 관계(orders, collections) 직렬화를 방지합니다.
 */
@Data
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 인수로 받는 생성자
public class PartnerModalDto {

    private String partnerCode; // 거래처 코드
    private String partnerName; // 거래처명
    private String partnerPhone; // 전화번호
    private String manager;      // 담당자
    private String name; // 담당자명
    private String phone; // 담당자 연락처
    private Integer postCode;    // 우편번호
    private String address;      // 주소
    
}