package com.yedam.hr.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.hr.domain.CommuteList;
import com.yedam.hr.service.CommuteListService;

@Controller
public class OnOffController {

	@Autowired
	CommuteListService commuteListService;

	@GetMapping("/onOffPage")
	public String getOnOffPage(Model model) {
		return "hr/onOff";
	}

	// 출근등록
	@PostMapping("/onoff/on")
	@ResponseBody
	public CommuteList checkin(@RequestBody CommuteList req) {
		return commuteListService.insertCommute(req);
	}

	// 퇴근등록
	@PostMapping("/onoff/off")
	@ResponseBody
	public java.util.Map<String, Object> checkout(@RequestBody CommuteList req) {
	    int updated = commuteListService.punchOutByDate(
	        req.getCompanyCode(),
	        req.getEmpCode(),
	        req.getOffTime()
	    );
	    return java.util.Map.of("updated", updated);
	}

	// 회사코드별 출퇴근 목록 조회
	@GetMapping("/onoff")
	@ResponseBody
	public List<CommuteList> getCommuteLists(@RequestParam String companyCode) {
		return commuteListService.getCommuteLists(companyCode);
	}
}
