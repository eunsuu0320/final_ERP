package com.yedam.hr.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.repository.SalaryProcedureRepository;
import com.yedam.hr.service.PayrollService;

import jakarta.transaction.Transactional;

@Service
public class PayrollServiceImpl implements PayrollService {

	@Autowired SalaryProcedureRepository salaryProcedureRepository;

	// 서비스 레이어에서 한 번 더 트랜잭션 보장
    @Override
    @Transactional
    public void runSalaryCalc(String companyCode, String salaryId) {
        if (companyCode == null || companyCode.isBlank()) {
            throw new IllegalArgumentException("companyCode는 필수입니다.");
        }
        if (salaryId == null || salaryId.isBlank()) {
            throw new IllegalArgumentException("salaryId는 필수입니다.");
        }
        salaryProcedureRepository.callPrEmpSalary(salaryId, companyCode);
    }
}
