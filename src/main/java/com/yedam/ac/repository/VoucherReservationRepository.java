package com.yedam.ac.repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Types;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class VoucherReservationRepository {

    private final JdbcTemplate jt;

    /** PR_RESERVE_VOUCHER 호출: 예약 생성 */
    public ReserveRes reserve(String companyCode, String kind, java.time.LocalDate baseDate, String userId) {
        final String sql = "{ call PR_RESERVE_VOUCHER(?, ?, ?, ?, ?, ?) }";
        return jt.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(sql)) {
                cs.setString(1, companyCode);
                cs.setString(2, kind); // 'SALES'/'BUY'/'RCV'/'PAY'
                cs.setDate(3, Date.valueOf(baseDate));
                cs.setString(4, userId);
                cs.registerOutParameter(5, Types.VARCHAR); // o_resv_id
                cs.registerOutParameter(6, Types.VARCHAR); // o_voucher_no
                cs.execute();
                return new ReserveRes(cs.getString(5), cs.getString(6));
            }
        });
    }

    /** 예약 단건 조회 (voucherNo 확인용) */
    public String findVoucherNoByResvId(String resvId) {
        return jt.query(
            "SELECT voucher_no FROM VOUCHER_RESERVATION WHERE resv_id = ? AND status = 'HELD'",
            ps -> ps.setString(1, resvId),
            rs -> rs.next() ? rs.getString(1) : null
        );
    }

    /** 예약 확정(사용 처리) */
    public int markUsed(String resvId) {
        return jt.update(
            "UPDATE VOUCHER_RESERVATION SET status = 'USED' WHERE resv_id = ? AND status = 'HELD'",
            ps -> ps.setString(1, resvId)
        );
    }

    /** 예약 취소(선택사항) */
    public int cancel(String resvId) {
        return jt.update(
            "UPDATE VOUCHER_RESERVATION SET status = 'CANCELLED' WHERE resv_id = ? AND status = 'HELD'",
            ps -> ps.setString(1, resvId)
        );
    }

    /** 결과 DTO */
    public record ReserveRes(String reservationId, String voucherNo) {}
}
