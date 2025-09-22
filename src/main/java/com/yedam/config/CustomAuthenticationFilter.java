package com.yedam.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        String username = obtainUsername(request);
        String password = obtainPassword(request);
        String companyCode = request.getParameter("companyCode");
        
        CustomAuthenticationFilter customFilter = new CustomAuthenticationFilter();
        customFilter.setFilterProcessesUrl("/login"); // 꼭 추가해야함!

        
        if (username == null) username = "";
        if (password == null) password = "";
        if (companyCode == null) companyCode = "";

        username = username.trim();

        // "회사코드:아이디" 형식으로 합쳐서 Principal로 넘김
        String combinedUsername = companyCode + ":" + username;

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(combinedUsername, password);

        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
