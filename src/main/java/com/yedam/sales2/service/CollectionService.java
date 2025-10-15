package com.yedam.sales2.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales2.domain.CollectionEntity;

public interface CollectionService {
	
	// 특정 수금 조회 (선택적)
    List<CollectionEntity> findByMoneyCode(String moneyCode);

    // 거래처별 미수금 현황 조회
    List<Map<String, Object>> getReceivableSummary(String companyCode);
    
    // 수금 등록
    void executeCollectionFifo(String partnerCode, Double paymentAmt, Double postDeduction, String paymentMethods, String remk, String companyCode);


}

