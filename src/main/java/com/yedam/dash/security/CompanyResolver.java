// src/main/java/com/yedam/dash/security/CompanyResolver.java
package com.yedam.dash.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CompanyResolver {

    public String resolveCompanyCode(HttpServletRequest req){
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() != null) {
                Object p = auth.getPrincipal();
                try {
                    var m = p.getClass().getMethod("getCompanyCode");
                    Object v = m.invoke(p);
                    if (v != null) return String.valueOf(v);
                } catch (NoSuchMethodException ignore){}
            }
        }catch(Exception ignore){}

        if (req.getSession(false) != null) {
            Object s = req.getSession(false).getAttribute("COMPANY_CODE");
            if (s != null) return s.toString();
        }

        String h = req.getHeader("X-Company-Code");
        if (h != null && !h.isBlank()) return h;
        String q = req.getParameter("companyCode");
        if (q != null && !q.isBlank()) return q;

        throw new IllegalStateException("회사 코드(COMPANY_CODE)를 확인할 수 없습니다.");
    }
}
