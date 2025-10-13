package com.yedam.common.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.common.service.UserService;

/*
 * 메인, 로그인 관리를 위한 컨트롤러 클래스
 * 박효준
 */
@Controller
public class HomeController {
	
	@Autowired UserService userService;
	
	@GetMapping("/")
	public String index(Model model) {
		return "index";
	}

	@GetMapping("/main")
	public String main(Model model) {
		return "layout/main";
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		return "dashboard";
	}

	@GetMapping("test")
	public String test(Model model) {
		return "test/test";
	}

	@GetMapping("/common/login")
	public String login(Model model) {
		return "common/login";
	}
	 
	@GetMapping("/subscription")
	public String subscription(Model model) {
		return "common/subscription";
	}
	
	@GetMapping("/subscriptionManager")
	public String subscriptionManager(Model model) {
		return "common/subscriptionManager";
	}
	
	@GetMapping("/authManager")
	public String authManager(Model model) {
		return "common/authManager";
	}
	
	@PostMapping("/common/findPassword")
	@ResponseBody
	public Map<String, Object> findPassword(@RequestBody Map<String, String> payload) {
	    String companyCode = payload.get("companyCode");
	    String userId = payload.get("userId");
	    String email = payload.get("email");

	    String result = userService.resetPassword(companyCode, userId, email);

	    Map<String, Object> response = new HashMap<>();
	    if ("SUCCESS".equals(result)) {
	        response.put("success", true);
	        response.put("message", "임시 비밀번호가 이메일로 발송되었습니다.");
	    } else if ("MAIL_ERROR".equals(result)) {
	        response.put("success", false);
	        response.put("message", "임시 비밀번호는 저장되었지만, 이메일 발송에 실패했습니다. 관리자에게 문의하세요.");
	    } else {
	        response.put("success", false);
	        response.put("message", "입력한 정보와 일치하는 계정이 없습니다.");
	    }
	    return response;
	} 
   
}
