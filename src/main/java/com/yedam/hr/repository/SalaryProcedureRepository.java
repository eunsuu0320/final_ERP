// src/main/java/com/yedam/payroll/repository/SalaryProcedureRepository.java
package com.yedam.hr.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class SalaryProcedureRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional // 호출 자체를 트랜잭션으로 감쌉니다.
    public void callPrEmpSalary(String salaryId, String companyCode) {
        StoredProcedureQuery spq = em.createStoredProcedureQuery("PR_EMP_SALARY");
        spq.registerStoredProcedureParameter(1, String.class, ParameterMode.IN); // P_SALARY_ID
        spq.registerStoredProcedureParameter(2, String.class, ParameterMode.IN); // P_COMPANY_CODE
        spq.setParameter(1, salaryId);
        spq.setParameter(2, companyCode);
        spq.execute(); // 결과셋/OUT 파라미터가 없으므로 실행만
    }
}
