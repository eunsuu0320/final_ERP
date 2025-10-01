package com.yedam.sales1.dto;

import com.yedam.sales1.domain.EstimateDetail;
import lombok.Data;
import java.time.LocalDate;
import java.util.List; // 리스트 임포트 필수

@Data
public class EstimateRegistrationDTO {
    // 1. 헤더 필드는 DTO의 최상위 필드로 직접 구성 (Estimate 엔티티 전체를 받는 것보다 깔끔함)
    private String partnerCode;
    private String partnerName;
    private LocalDate quoteDate; 
    private Integer validPeriod; 
    private String remarks;
    private String manager;
    
    // 2. 상세 항목은 리스트(배열)로 받습니다. (N개의 항목 처리)
    private List<EstimateDetail> detailList; 
}