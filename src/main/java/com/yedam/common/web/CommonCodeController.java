package com.yedam.common.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.common.domain.CommonCode;
import com.yedam.common.service.CommonCodeService;

@Controller
public class CommonCodeController {

	@Autowired CommonCodeService commonCodeService;

	@ResponseBody
	@GetMapping("/api/modal/commonCode")
	public List<CommonCode> commonCode(String groupId) {
		return commonCodeService.findByGroupId(groupId);
	}

	@ResponseBody
	@GetMapping("/api/modal/commonCodes")
	public Map<String, List<CommonCode>> commonCodes(String groupId) {
		return commonCodeService.getCodes(groupId);
	}
}
