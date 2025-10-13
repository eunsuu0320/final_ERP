package com.yedam.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.common.domain.Company;

@Repository
public interface PaymentRepository extends JpaRepository<Company, String> {
	@Query(value = "SELECT * FROM COMPANY WHERE BIZ_REG_NO = :bizRegNo AND ROWNUM = 1", nativeQuery = true)
    Optional<Company> findAnyByBizRegNo(@Param("bizRegNo") String bizRegNo);

    boolean existsByBizRegNo(String bizRegNo);
}
