package com.yedam.common.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.common.domain.Subscription;
import com.yedam.common.domain.SystemUser;
import com.yedam.common.repository.SubscriptionRepository;
import com.yedam.common.repository.UserRepository;

@Controller
public class SubscriptionController {
		
	@Autowired UserRepository userRepository;
	@Autowired SubscriptionRepository subscriptionRepository;
	
	// 사용자 목록 조회
	@ResponseBody
	@GetMapping("/api/common/users")
	public List<SystemUser> userList(@RequestParam(required=false) String kw,
            						 @RequestParam(required=false) String dept,
            						 @RequestParam(required=false) String useYn) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    String companyCode = auth.getName().split(":")[0];
	    
		return userRepository.findUserByCompanyCode(companyCode, kw, dept, useYn);
	}
	
	// 구독내역 조회
	@ResponseBody
	@GetMapping("/api/common/subs")
	public List<Subscription> subscriptionList() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    String companyCode = auth.getName().split(":")[0];
	    
		return subscriptionRepository.findByCompanyCode(companyCode);
	}
}
