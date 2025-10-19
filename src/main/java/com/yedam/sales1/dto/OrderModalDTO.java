package com.yedam.sales1.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderModalDTO {
    private Long orderUniqueCode;
    private String orderCode;
    private Long estimateUniqueCode;
    private String estimateCode; // 프런트 표시용

    private String partnerCode;
    private String partnerName;

    private String manager;
    private String managerName;

    private Integer postCode;      // 🆕
    private String address;        // 🆕
    private String payCondition;   // 🆕

    private Date deliveryDate;
    private String remarks;

    private List<OrderDetailDTO> detailList;
}
