package com.yedam.sales1.dto;

import com.yedam.sales1.domain.ShipmentDetail; // ShipmentDetail 엔티티를 직접 사용
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentRegistrationDTO {
    
    // =============================================================
    // Shipment (마스터) 정보 필드
    // =============================================================

    private String shipmentDate; 
    
    private String partnerName; 
    private String partnerCode; 
    
    private String warehouse;
    
    private String manager;
    
    private String postCode; 
    private String address;  
    
    private String remarks;
    
    // =============================================================
    // ShipmentDetail (상세) 정보 리스트 필드 (엔티티 자체를 사용)
    // =============================================================
    // ⭐ ShipmentDetail 엔티티를 리스트 타입으로 사용합니다.
    private List<ShipmentDetail> detailList; 
}