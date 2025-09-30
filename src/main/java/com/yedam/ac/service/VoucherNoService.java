package com.yedam.ac.service;

import java.time.LocalDate;

import com.yedam.ac.repository.VoucherReservationRepository.ReserveRes;

public interface VoucherNoService {

    /** 종류별(yymm-####) 다음 번호 프리뷰 — DB 변경 없음 */
    String previewNext(String kind, LocalDate baseDate);

    /** 실제 예약(발번 확정) — 저장 직전에만 호출 */
    ReserveRes reserve(String kind, LocalDate baseDate, String userId);
}
