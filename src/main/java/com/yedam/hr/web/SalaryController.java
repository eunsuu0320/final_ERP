package com.yedam.hr.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.hr.domain.SalaryDetail;
import com.yedam.hr.domain.SalaryMaster;
import com.yedam.hr.service.SalaryDetailService;
import com.yedam.hr.service.SalaryMasterService;

@Controller
public class SalaryController {

	@Autowired
	SalaryMasterService salaryMasterService;
	@Autowired
	SalaryDetailService salaryDetailService;

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

	// 급여대장 명세서 조회 모달 페이지
	@GetMapping("/salaryPay")
	public String getPaySelect(Model model, @RequestParam String salaryId) {
		 Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		    String companyCode = auth.getName().split(":")[0];
		    model.addAttribute("list", salaryDetailService.getSalaryDetailBundle(companyCode, salaryId));
		return "hr/salaryPay";
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

	// 급여마스터 저장
	@PostMapping("/salaryMaster/save")
	@ResponseBody
	public String saveSalaryMaster(@RequestBody SalaryMaster salaryMaster) {
		try {
			salaryMasterService.insertSalaryMaster(salaryMaster);
			return "success";
		} catch (Exception e) {
			return "fail: " + e.getMessage();
		}
	}

	// 급여대장 확정
	@PostMapping("/api/payroll/confirm")
	public ResponseEntity<Map<String, Object>> confirmSelected(@RequestBody Map<String, Object> body) {
		String companyCode = (String) body.get("companyCode");
		Object idsObj = body.get("salaryIds");

		if (companyCode == null || idsObj == null || !(idsObj instanceof List<?> list) || list.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "companyCode 또는 salaryIds가 비어 있음"));
		}

		List<String> salaryIds = new ArrayList<>(list.size());
		for (Object o : list) {
			if (o != null)
				salaryIds.add(String.valueOf(o));
		}

		int updated = salaryMasterService.confirmSelected(companyCode, salaryIds);
		return ResponseEntity.ok(Map.of("updated", updated));
	}

}
