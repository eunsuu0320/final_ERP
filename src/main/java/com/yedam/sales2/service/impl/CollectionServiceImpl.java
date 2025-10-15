package com.yedam.sales2.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.sales2.domain.CollectionEntity;
import com.yedam.sales2.repository.CollectionRepository;
import com.yedam.sales2.service.CollectionService;

@Service
public class CollectionServiceImpl implements CollectionService{

	@Autowired
	private CollectionRepository collectionRepository;
	
	// 수금조회
	 @Override
	    public List<CollectionEntity> findByMoneyCode(String moneyCode) {
	        return collectionRepository.findByMoneyCode(moneyCode);
	    }
	 
	// 거래처별 미수금 현황 조회
	    @Override
	    public List<Map<String, Object>> getReceivableSummary(String companyCode) {
	        return collectionRepository.findReceivableSummary(companyCode);
	    }
	    
	 // 수금 등록
	    @Override
	    @Transactional
	    public void executeCollectionFifo(String partnerCode, Double paymentAmt,  Double postDeduction, String paymentMethods, String remk, String companyCode) {
	        collectionRepository.callCollectionFifoProc(partnerCode, paymentAmt, postDeduction, paymentMethods, remk, companyCode);
	    }
}
