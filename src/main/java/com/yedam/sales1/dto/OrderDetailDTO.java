package com.yedam.sales1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderDetailDTO {
    private String productCode;
    private String productName;
    private String productSize; // spec 구성용
    private String unit;
    private Integer quantity;
    private Double price;
    private Double amountSupply;
    private Double pctVat;
    private String remarks;
}
