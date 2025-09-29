package com.yedam.hr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.HrHistory;

@Repository
public interface HrHistoryRepository extends JpaRepository<HrHistory, Long>{

}
