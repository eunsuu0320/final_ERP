// src/main/java/com/yedam/ac/repository/VoucherNoRepositoryJdbc.java
package com.yedam.ac.repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class VoucherNoRepositoryJdbc implements VoucherNoRepository {

    private final JdbcTemplate jt;

    @Override
    public String next(String companyCode, String kind, LocalDate baseDate) {
        // 함수가 SYSTEM 스키마에 있으면 "SYSTEM.FN_NEXT_VOUCHER_NO" 로 명시해도 됩니다.
        final String sql = "{ ? = call FN_NEXT_VOUCHER_NO(?, ?, ?) }";
        return jt.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.registerOutParameter(1, Types.VARCHAR);
                cs.setString(2, companyCode);
                cs.setString(3, kind);                       // 'SALES' / 'BUY' / 'RCV' / 'PAY'
                cs.setDate(4, Date.valueOf(baseDate));       // 기준일(전표일자)
                cs.execute();
                return cs.getString(1);
            }
        });
    }
}
