package com.yedam.ac.config;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.yedam.ac.util.CompanyContext;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HibernateFilterEnabler implements HandlerInterceptor {

    private final EntityManager em;
    private final CompanyContext companyCtx;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String cc = companyCtx.getCompanyCode();
        if (cc != null && !cc.isBlank()) {
            Session session = em.unwrap(Session.class);
            Filter f = session.enableFilter("companyFilter");
            f.setParameter("cc", cc);
        }
        return true;
    }
}
