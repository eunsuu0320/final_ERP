package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.repository.EstimateRepository;
import com.yedam.sales1.service.EstimateService;

import jakarta.transaction.Transactional;

@Service
public class EstimateServiceImpl implements EstimateService {

    private final EstimateRepository estimateRepository;

    @Autowired
    public EstimateServiceImpl(EstimateRepository estimateRepository) {
        this.estimateRepository = estimateRepository;
    }

    @Override
    public List<Estimate> getAllEstimate() {
        return estimateRepository.findAll();
    }

    @Override
    public Map<String, Object> getTableDataFromEstimate(List<Estimate> estimates) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        if (!estimates.isEmpty()) {
            // 컬럼 정의
            columns.add("견적서코드");
            columns.add("등록일자");
            columns.add("거래처명");
            columns.add("품목명");
            columns.add("유효기간");
            columns.add("견적금액합계");
            columns.add("담당자");
            columns.add("진행상태");

            for (Estimate estimate : estimates) {
                Map<String, Object> row = new HashMap<>();
                row.put("견적서코드", estimate.getEstimateCode());
                row.put("등록일자", estimate.getCreateDate());
                row.put("거래처명", estimate.getPartnerCode());
                row.put("품목명", estimate.getPartnerCode());
                row.put("유효기간", estimate.getExpiryDate());
                row.put("견적금액합계", estimate.getTotalAmount());
                row.put("담당자", estimate.getManager());
                row.put("진행상태", estimate.getStatus());
                rows.add(row);
            }
        }

        return Map.of("columns", columns, "rows", rows);
    }

    @Override
    @Transactional
    public Estimate saveEstimate(Estimate estimate) {
        return estimateRepository.save(estimate);
    }


}
