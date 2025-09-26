package com.yedam.sales2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.yedam.sales2.domain.DSalesPlan;

@Repository
public interface DSalesPlanRepository extends JpaRepository<DSalesPlan, Integer> { }
