package com.yedam.hr.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.hr.dto.HrHistoryDTO;
import com.yedam.hr.service.HrHistorySerivce;

@Controller
public class HrHistoryController {

	@Autowired HrHistorySerivce hrHistorySerivce;

	@ResponseBody
	@GetMapping("/api/history/{companyCode}")
	public List<HrHistoryDTO> getHistoryByEmpNo(@PathVariable String companyCode) {
		return hrHistorySerivce.findByCompanyCode(companyCode);
	}
}
