package com.yedam.sales1.dto;

import java.util.List;

import com.yedam.sales1.domain.ShipmentDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentRegistrationDTO {

    // 출하 마스터(Shipment)
    private String shipmentDate;      // 출하예정일자
    private String partnerName;       // 거래처명
    private String partnerCode;       // 거래처코드
    private String warehouse;         // 창고
    private String manager;           // 담당자
    private String postCode;          // 우편번호
    private String address;           // 주소
    private String remarks;           // 비고

    // 출하 상세(ShipmentDetail)
    private List<ShipmentDetail> detailList; // 상세 항목
}
