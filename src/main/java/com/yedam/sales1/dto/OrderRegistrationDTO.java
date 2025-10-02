package com.yedam.sales1.dto;

import java.time.LocalDate;
import java.util.List;

import com.yedam.sales1.domain.OrderDetail;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

// OrdersServiceImpl에서 .builder()를 사용하므로 @Builder와 @AllArgsConstructor를 추가합니다.
@Data
@NoArgsConstructor // Lombok을 사용하더라도 기본 생성자는 명시적으로 추가하는 것이 좋습니다.
@AllArgsConstructor
@Builder 
public class OrderRegistrationDTO {
    
    // 1. OrdersServiceImpl에서 getPartnerCode(), getPartnerName(), getManager(), getRemarks() 사용
    private String partnerCode;
    private String partnerName;
    private String manager;
    private String remarks;

    // 2. OrdersServiceImpl에서 getOrderDate() 사용을 위해 quoteDate -> orderDate로 수정
    private LocalDate orderDate; 
    
    // 3. OrdersServiceImpl에서 getDeliveryDate() 사용을 위해 추가
    private LocalDate deliveryDate;

    // 4. OrdersServiceImpl에서 getEstimateUniqueCode() 사용을 위해 추가 (연결된 견적서 ID)
    private Long estimateUniqueCode; 
    
    // 5. OrdersServiceImpl에서 getDetailList() 사용
    private List<OrderDetail> detailList;
    
    // 이전 DTO에 있던 validPeriod는 주문서 등록 로직에 필요하지 않아 제거했습니다.
}