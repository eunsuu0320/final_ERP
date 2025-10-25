package com.yedam.sales1.dto;

import java.util.Date;
import java.util.List;

import com.yedam.sales1.domain.OrderDetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderModalDTO {

    private Long orderUniqueCode;
    private String orderCode;
    private Long estimateUniqueCode;
    private String estimateCode; // 프런트 표시용

    private String partnerCode;
    private String partnerName;

    private String manager;
    private String managerName;

    private Integer postCode;
    private String address;
    private String payCondition;

    private Date deliveryDate;
    private Date createDate;

    private String remarks;
    private Double totalAmount;

    // ✅ 주문 상세 리스트 (ORDER_DETAIL)
    private List<OrderDetail> detailList;

    // ✅ 테이블에 존재하지 않는 계산용 필드
    private Integer completeQuantity; // (quantity - nonShipment)
    private Integer stock; // 상품 재고 (Product 테이블에서 가져옴)
}
