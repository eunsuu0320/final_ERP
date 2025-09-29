package com.yedam.common.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.yedam.common.domain.payment.PayRequest;

@Repository
public class PaymentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertCompany(PayRequest request) {
        String sql = "INSERT INTO COMPANY " +
                "(COMPANY_CODE, BIZ_REG_NO, COMPANY_NAME, CEO_NAME, ADDRESS, TEL, CREATED_DATE, REMK) " +
                "VALUES (?, ?, ?, ?, ?, ?, SYSDATE, ?)";

        jdbcTemplate.update(sql,
                "C" + System.currentTimeMillis(),   // COMPANY_CODE 생성 규칙
                request.getBizRegNo(),
                request.getCompanyName(),
                request.getCeoName(),
                request.getAddress(),
                request.getAddressDetail(),
                request.getTel(),
                "ERP 구독 결제 완료"                     // REMK
        );
    }
}
