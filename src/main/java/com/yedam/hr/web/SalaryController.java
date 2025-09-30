package com.yedam.hr.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.hr.domain.SalaryDetail;
import com.yedam.hr.domain.SalaryMaster;
import com.yedam.hr.service.SalaryDetailService;
import com.yedam.hr.service.SalaryMasterService;

import ch.qos.logback.core.model.Model;

@Controller
public class SalaryController {

	@Autowired SalaryMasterService salaryMasterService;
	@Autowired SalaryDetailService salaryDetailService;

	// 급여대장 페이지
	@GetMapping("/salaryPage")
	public String getSalary(Model model) {
		return "hr/salaryMaster";
	}

	// 급여대장 신규 모달 페이지
	@GetMapping("/salaryModal")
	public String getSalaryModal(Model model) {
		return "hr/salaryModal";
	}

	// 급여마스터 회사코드별 조회 목록
	@GetMapping("/salaryMaster")
	@ResponseBody
	public List<SalaryMaster> getSalaryMasters(@RequestParam String companyCode) {
		return salaryMasterService.getSalaryMasters(companyCode);
	}

	// 급여디테일 회사코드별 조회 목록
	@GetMapping("/salaryDetail")
	@ResponseBody
	public List<SalaryDetail> getSalaryDetails(@RequestParam String companyCode) {
		return salaryDetailService.getSalaryDetails(companyCode);
	}

}
