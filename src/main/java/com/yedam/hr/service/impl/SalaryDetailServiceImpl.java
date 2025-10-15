package com.yedam.hr.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.SalaryDetail;
import com.yedam.hr.repository.SalaryDetailRepository;
import com.yedam.hr.service.SalaryDetailService;
import com.yedam.hr.service.SalaryMasterService;

@Service
public class SalaryDetailServiceImpl implements SalaryDetailService {

	@Autowired SalaryDetailRepository salaryDetailRepository;
	@Autowired SalaryMasterService salaryMasterService;

	@Override
	public List<SalaryDetail> getSalaryDetails(String companyCode) {
		return salaryDetailRepository.findByCompanyCode(companyCode);
	}

	@Override
	public Map<String, Object> getSalaryDetailBundle(String companyCode, String salaryId) {
	    // 1) SalaryDetail 전체 조회
	    List<SalaryDetail> details =
	        salaryDetailRepository.findByCompanyCodeAndSalaryId(companyCode, salaryId);

	    // 2) 합계 변수(long)
	    long all01=0, all02=0, all03=0, all04=0, all05=0, all06=0, all07=0, all08=0, all09=0, all10=0;
	    long ded01=0, ded02=0, ded03=0, ded04=0, ded05=0, ded06=0, ded07=0, ded08=0, ded09=0, ded10=0;
	    long payTotalSum=0, dedTotalSum=0, netPaySum=0;

	    // 3) 누적
	    for (SalaryDetail sd : details) {
	        all01 += sd.getAll01();
	        all02 += sd.getAll02();
	        all03 += sd.getAll03();
	        all04 += sd.getAll04();
	        all05 += sd.getAll05();
	        all06 += sd.getAll06();
	        all07 += sd.getAll07();
	        all08 += sd.getAll08();
	        all09 += sd.getAll09();
	        all10 += sd.getAll10();

	        ded01 += sd.getDed01();
	        ded02 += sd.getDed02();
	        ded03 += sd.getDed03();
	        ded04 += sd.getDed04();
	        ded05 += sd.getDed05();
	        ded06 += sd.getDed06();
	        ded07 += sd.getDed07();
	        ded08 += sd.getDed08();
	        ded09 += sd.getDed09();
	        ded10 += sd.getDed10();

	        payTotalSum += sd.getAllTotal();
	        dedTotalSum += sd.getDedTotal();
	        netPaySum   += sd.getNetPay();
	    }

	    // 4) 합계 맵
	    Map<String, Long> allSums = new LinkedHashMap<>();
	    allSums.put("ALL_01", all01);
	    allSums.put("ALL_02", all02);
	    allSums.put("ALL_03", all03);
	    allSums.put("ALL_04", all04);
	    allSums.put("ALL_05", all05);
	    allSums.put("ALL_06", all06);
	    allSums.put("ALL_07", all07);
	    allSums.put("ALL_08", all08);
	    allSums.put("ALL_09", all09);
	    allSums.put("ALL_10", all10);

	    Map<String, Long> dedSums = new LinkedHashMap<>();
	    dedSums.put("DED_01", ded01);
	    dedSums.put("DED_02", ded02);
	    dedSums.put("DED_03", ded03);
	    dedSums.put("DED_04", ded04);
	    dedSums.put("DED_05", ded05);
	    dedSums.put("DED_06", ded06);
	    dedSums.put("DED_07", ded07);
	    dedSums.put("DED_08", ded08);
	    dedSums.put("DED_09", ded09);
	    dedSums.put("DED_10", ded10);

	    // 5) payYm (있으면)
	    String payYm = salaryMasterService.findByCompanyCodeAndSalaryId(companyCode, salaryId);
	    System.out.println(payYm);

	    // 6) 반환
	    // 급여대장 마스터 전체 넘기기 어떻게?

	    Map<String, Object> result = new LinkedHashMap<>();
	    result.put("companyCode", companyCode);
	    result.put("salaryId",    salaryId);
	    result.put("payYm",       payYm);
	    result.put("details",     details);
	    result.put("allSums",     allSums);
	    result.put("dedSums",     dedSums);
	    result.put("payTotalSum", payTotalSum);
	    result.put("dedTotalSum", dedTotalSum);
	    result.put("netPaySum",   netPaySum);
	    return result;
	}
}
