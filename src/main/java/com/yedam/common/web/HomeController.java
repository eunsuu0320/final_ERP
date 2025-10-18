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

import com.yedam.common.ScreenPerm;
import com.yedam.common.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

/*
 * 메인, 로그인 관리를 위한 컨트롤러 클래스
 * 박효준
 */
@Controller
public class HomeController {

	@Autowired
	UserService userService;

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

	@GetMapping("/common/login")
	public String login(Model model) {
		return "common/login";
	}

	@GetMapping("/subscription")
	public String subscription(Model model) {
		return "common/subscription";
	}

	@ScreenPerm(screen = "COM_SUB", action = ScreenPerm.Action.READ)
	@GetMapping("/subscriptionManager")
	public String subscriptionManager(Model model) {
		return "common/subscriptionManager";
	}
	
	@ScreenPerm(screen = "COM_ROLE", action = ScreenPerm.Action.READ)
	@GetMapping("/authManager")
	public String authManager(Model model) {
		return "common/authManager";
	}
	
	@ScreenPerm(screen = "COM_CODE", action = ScreenPerm.Action.READ)
	@GetMapping("/commonCodeManager")
	public String commonCodeManager(Model model) {
		return "common/commonCodeManager";
	}

	@GetMapping("/forbidden")
	public String forbidden(HttpServletRequest request, Model model) {
		// SecurityConfig.accessDeniedHandler에서 넣어준 메시지
		Object msg = request.getAttribute("forbiddenMessage");
		String message = (msg instanceof String && !((String) msg).isBlank()) ? (String) msg : "이 페이지에 접근할 권한이 없습니다.";

		// 포워드된 원래 요청 경로 가져오기 (없으면 현재 URI)
		String fwd = (String) request.getAttribute("jakarta.servlet.forward.request_uri");
		if (fwd == null) {
			fwd = request.getRequestURI();
		}

		model.addAttribute("message", message);
		model.addAttribute("reqPath", fwd);
		return "common/forbidden";
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
