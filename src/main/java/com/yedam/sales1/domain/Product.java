package com.yedam.sales1.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Data
@Entity
@Table(name = "PRODUCT") // 실제 테이블 이름이 PRODUCT라면 추가하는 것이 좋습니다.
public class Product {
    @Id
    @Column(name = "PRODUCT_CODE")
    private String productCode;      // 상품코드

    @Column(name = "PRODUCT_NAME")
    private String productName;      // 상품명
    
    @Column(name = "PRODUCT_GROUP")
    private String productGroup;     // 상품그룹
    
    @Column(name = "PRODUCT_SIZE")
    private String productSize;      // 상품규격
    
    @Column(name = "UNIT")
    private String unit;             // 단위
    
    @Column(name = "IMG_PATH")
    private String imgPath;          // 이미지 경로
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_DATE")
    private Date createDate;         // 생성일자
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATE_DATE")
    private Date updateDate;         // 수정일자
    
    @Column(name = "REMARKS")
    private String remarks;          // 비고
    
    @Column(name = "USAGE_STATUS")
    private String usageStatus;      // 사용여부 (Y/N)
    
    @Column(name = "COMPANY_CODE")
    private String companyCode;      // 회사코드
    
    @Column(name = "WAREHOUSE_CODE")
    private String warehouseCode;    // 창고코드
    
    @Column(name = "STOCK")
    private Integer stock;           // 재고

    // Jpa의 PrePersist 이벤트를 사용하여 INSERT 시 자동으로 생성일자를 설정
    @PrePersist
    protected void onCreate() {
        this.createDate = new Date();
        this.updateDate = new Date();
    }
    
    // Jpa의 PreUpdate 이벤트를 사용하여 UPDATE 시 자동으로 수정일자를 설정
    @PreUpdate
    protected void onUpdate() {
        this.updateDate = new Date();
    }
}