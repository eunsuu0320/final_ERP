package com.yedam.common.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
	public List<CommonCode> commonCode(String commonGroup) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    String companyCode;

	    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
	        // 로그인 안 된 경우
	        companyCode = "admin";
	    } else {
	        // 로그인 된 경우
	        companyCode = auth.getName().split(":")[0];
	    }
		return commonCodeService.findByGroupIdAndCompanyCode(commonGroup, companyCode);
	}

	@ResponseBody
	@GetMapping("/api/modal/commonCodes")
	public Map<String, List<CommonCode>> commonCodes(String groupId) {
		return commonCodeService.getCodes(groupId);
	}
}
