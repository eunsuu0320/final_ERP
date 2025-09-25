package com.yedam.hr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.HrSign;

@Repository
public interface HrSignRepository extends JpaRepository<HrSign, Long>{

}
