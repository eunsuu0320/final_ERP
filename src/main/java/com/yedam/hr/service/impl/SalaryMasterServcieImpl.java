// SalaryMasterServcieImpl.java
package com.yedam.hr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;  // << 추가

import com.yedam.hr.domain.SalaryMaster;
import com.yedam.hr.repository.SalaryDetailRepository;
import com.yedam.hr.repository.SalaryMasterRepository;
import com.yedam.hr.service.SalaryMasterService;

@Service
public class SalaryMasterServcieImpl implements SalaryMasterService {

	@Autowired SalaryMasterRepository salaryMasterRepository;
	@Autowired SalaryDetailRepository salaryDetailRepository;

	@Override
	public List<SalaryMaster> getSalaryMasters(String companyCode) {
		return salaryMasterRepository.findByCompanyCodeOrderBySalaryIdDesc(companyCode);
	}

	@Override
	@Transactional  // << 추가: 마스터+디테일 한 트랜잭션으로
	public SalaryMaster insertSalaryMaster(SalaryMaster salaryMaster) {
		// 로그인 사용자에서 회사코드 주입
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String companyCode = auth.getName().split(":")[0];
		salaryMaster.setCompanyCode(companyCode);
		salaryMaster.setConfirmIs("n");

		// payName 기본값 생성
	    if (salaryMaster.getPayName() == null || salaryMaster.getPayName().isEmpty()) {
	        String ym = salaryMaster.getPayYm();
	        String type = salaryMaster.getPayType();
	        String typeLabel = "1".equals(type) ? "1차" : ("2".equals(type) ? "2차" : "지급");
	        if (ym != null && ym.length() >= 7) {
	            salaryMaster.setPayName(ym.substring(0,7) + " 급여대장(" + typeLabel + ")");
	        } else {
	            salaryMaster.setPayName("급여대장");
	        }
	    }

		// 1) 급여대장 마스터 저장
	    SalaryMaster saved = salaryMasterRepository.save(salaryMaster);

	    // ▼ salaryId가 문자열
	    int created = salaryDetailRepository.insertDetailsForMaster(saved.getSalaryId(), companyCode);

		// 필요하면 created 값 로깅/검증
		// System.out.println("created salary details: " + created);

		return saved;
	}
}
