package com.yedam.ac.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SalesLookupDao {

    private final EntityManager em;

    @Transactional(readOnly = true)
    public long countByCodeAndCompany(String salesCode, String companyCode) {
        Number n = (Number) em.createNativeQuery(
                "SELECT COUNT(1) FROM SALES WHERE SALES_CODE = :code AND COMPANY_CODE = :cc")
            .setParameter("code", salesCode)
            .setParameter("cc", companyCode)
            .getSingleResult();
        return n.longValue();
    }
}
