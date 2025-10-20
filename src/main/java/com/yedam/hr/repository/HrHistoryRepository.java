package com.yedam.hr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.HrHistory;

@Repository
public interface HrHistoryRepository extends JpaRepository<HrHistory, Long> {

    // 기존: 전체 전량 조회
    List<HrHistory> findByCompanyCodeOrderByCreatedAtDesc(String companyCode);
}
