// src/main/java/com/yedam/ac/web/StatementPageController.java
package com.yedam.ac.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.yedam.ac.util.CompanyContext;

@Controller
//@RequestMapping("/ac")
public class StatementPageController {

    /** 일반전표 등록 페이지 */
    @GetMapping("/register")
    public String registerPage(HttpServletRequest req) {
        seedCompanyCodeFromRequest(req);
        return "ac/register"; // templates/ac/register.html
    }

    /** (선택) 통합 목록을 /ac/list 로도 접근하고 싶다면 */
    @GetMapping("/list")
    public String listPage(HttpServletRequest req) {
        seedCompanyCodeFromRequest(req);
        return "ac/statement-list"; // 기존 목록 템플릿을 재사용할 경우
    }

    /** 최초 진입 시 회사코드 세션 심기 (개발/테스트 편의) */
    private void seedCompanyCodeFromRequest(HttpServletRequest req) {
        HttpSession ses = req.getSession(true);
        if (ses.getAttribute(CompanyContext.ATTR) != null) return;

        String cc = req.getHeader("X-Company-Code");
        if (cc == null || cc.isBlank()) cc = req.getParameter("cc");
        if (cc != null && !cc.isBlank()) ses.setAttribute(CompanyContext.ATTR, cc.trim());
    }
}
