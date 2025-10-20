package com.yedam.sales1.dto;

import java.util.Date;
import java.util.List;

import com.yedam.sales1.domain.EstimateDetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 인수로 받는 생성자
public class EstimateModalDto {
	
	private Long estimateUniqueCode; // 견적서 식별 코드
	private String estimateCode; // 견적서코드
    private String partnerCode; // 거래처 코드
    private String partnerName; // 거래처명
    private String manager;      // 담당자
    private String managerName; // 담당자명
    private Integer postCode;    // 우편번호
    private String address;      // 주소
    private Date deliveryDate; // 납기일
    private Double totalAmount; // 견적금액합산
    private String remarks; // 비
    
    private List<EstimateDetail> detailList; // 견적서 식별코드로 조인할 견적서 디테일 테이블
    
}