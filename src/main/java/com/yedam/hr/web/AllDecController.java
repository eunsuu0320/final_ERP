package com.yedam.hr.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.hr.domain.Allowance;
import com.yedam.hr.service.AllowanceService;

@Controller
public class AllDecController {

	@Autowired AllowanceService allowanceService;

	// 수당 및 공제 관리
	@GetMapping("/allowDeduct")
	public String getAllowDeduct(Model model) {
		return "hr/allowDeduct";
	}

	// 수당 회사코드별 조회목록
	@GetMapping("/allowance")
	@ResponseBody
	public List<Allowance> getAllowances(@RequestParam String companyCode) {
		return allowanceService.findByCompanyCode(companyCode);
	}

	// 여러 건 저장
	@PostMapping("/allowance/saveAll")
	@ResponseBody
	public String saveAll(@RequestBody List<Allowance> allowances,
	                      @RequestParam String companyCode) {
	    try {
	        allowanceService.saveAllAllowances(allowances, companyCode);
	        return "success";
	    } catch (Exception e) {
	        return "fail";
	    }
	}

	@PostMapping("/allowance/updateStatus")
	@ResponseBody
	public String updateStatus(@RequestBody Map<String, Object> payload,
	                           @RequestParam String companyCode) {
	    List<Integer> codes = (List<Integer>) payload.get("codes");
	    String status = (String) payload.get("status");

	    try {
	        allowanceService.updateStatus(codes, status, companyCode);
	        return "success";
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "fail: " + e.getMessage();
	    }
	}

}
