package com.yedam.domain;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Product {
	@Id
    private String productCode;     // 상품코드
	
    private String productName;     // 상품명
    
    private String productGroup;    // 상품그룹
    
    private String productSize;     // 상품규격
    
    private String unit;            // 단위
    
    private String imgPath;         // 이미지 경로
    
    private Date createDate;        // 생성일자
    
    private Date updateDate;        // 수정일자
    
    private String remarks;         // 비고
    
    private String usageStatus;     // 사용여부 (Y/N)
    
    private String companyCode;     // 회사코드
    
    private String warehouseCode;   // 창고코드
    
    private Integer stock;          // 재고
}
