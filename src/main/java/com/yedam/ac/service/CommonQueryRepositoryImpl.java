package com.yedam.ac.service;

import org.springframework.stereotype.Repository;

import com.yedam.ac.repository.CommonQueryRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommonQueryRepositoryImpl implements CommonQueryRepository {
    private final EntityManager em;

    @Override
    public String nextVoucherNo() {
        // 필요 시 사업장/회사코드 포함 규칙으로 확장
        Object v = em.createNativeQuery(
            "SELECT TO_CHAR(SYSDATE,'YYYY')||'-'||LPAD(STATEMENT_SEQ.NEXTVAL,4,'0') FROM dual"
        ).getSingleResult();
        return String.valueOf(v);
    }
}