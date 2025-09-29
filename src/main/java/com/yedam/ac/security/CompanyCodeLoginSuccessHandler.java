// src/main/java/com/yedam/ac/security/CompanyCodeLoginSuccessHandler.java
package com.yedam.ac.security;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.yedam.ac.util.CompanyContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CompanyCodeLoginSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
            throws IOException {
        // TODO: 실제 로그인 사용자에서 회사코드 추출
        // 예) String companyCode = ((MyUser)auth.getPrincipal()).getCompanyCode();
        String companyCode = "C001"; // 임시(테스트)
        request.getSession(true).setAttribute(CompanyContext.ATTR, companyCode);
        response.sendRedirect("/statements");
    }
}
