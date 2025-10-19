package com.yedam.hr.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.HrHistory;
import java.util.List;

@Repository
public interface HrHistoryRepository extends JpaRepository<HrHistory, Long> {

    // 기존: 전체 전량 조회
    List<HrHistory> findByCompanyCodeOrderByCreatedAtDesc(String companyCode);

    // 추가: 서버 페이지네이션(스키마 변경 없음)
    Page<HrHistory> findByCompanyCodeOrderByCreatedAtDesc(String companyCode, Pageable pageable);
}
