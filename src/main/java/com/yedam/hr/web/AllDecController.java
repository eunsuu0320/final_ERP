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
import com.yedam.hr.domain.Dedcut;
import com.yedam.hr.service.AllowanceService;
import com.yedam.hr.service.DedcutService;

@Controller
public class AllDecController {

	@Autowired AllowanceService allowanceService;
	@Autowired DedcutService dedcutService;

	// 수당 및 공제 관리 페이지
	@GetMapping("/allowDeduct")
	public String getAllowDeduct(Model model) {
		return "hr/allowDeduct";
	}

	// 수당 회사코드별 조회 목록
	@GetMapping("/allowance")
	@ResponseBody
	public List<Allowance> getAllowances(@RequestParam String companyCode) {
		return allowanceService.findByCompanyCode(companyCode);
	}

	// 공제 회사코드별 조회 목록
	@GetMapping("/dedcut")
	@ResponseBody
	public List<Dedcut> getDedcuts(@RequestParam String companyCode) {
		return dedcutService.findByCompanyCode(companyCode);
	}

	// 수당 여러 건 저장
	@PostMapping("/allowance/saveAll")
	@ResponseBody
	public String saveAllowanceAll(@RequestBody List<Allowance> allowances,
	                      @RequestParam String companyCode) {
	    try {
	        allowanceService.saveAllAllowances(allowances, companyCode);
	        return "success";
	    } catch (Exception e) {
	        return "fail";
	    }
	}

	// 공제 여러 건 저장
	@PostMapping("/dedcut/saveAll")
	@ResponseBody
	public String saveDedcutAll(@RequestBody List<Dedcut> dedcuts,
			  			        @RequestParam String companyCode) {
		try {
			dedcutService.saveAllDedcuts(dedcuts, companyCode);
			return "success";
		} catch (Exception e) {
			return "fail";
		}
	}

	// 수당
	@PostMapping("/allowance/updateStatus")
	@ResponseBody
	public String allUpdateStatus(@RequestBody Map<String, Object> payload,
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

	// 공제
	@PostMapping("/dedcut/updateStatus")
	@ResponseBody
	public String dedcutUpdateStatus(@RequestBody Map<String, Object> payload, @RequestParam String companyCode) {
		List<Integer> codes = (List<Integer>) payload.get("codes");
		String status = (String) payload.get("status");

		try {
			dedcutService.updateStatus(codes, status, companyCode);
			return "success";
		} catch (Exception e) {
			e.printStackTrace();
			return "fail: " + e.getMessage();
		}
	}

}
