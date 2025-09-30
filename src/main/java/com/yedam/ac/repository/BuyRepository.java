package com.yedam.ac.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.domain.Buy;

public interface BuyRepository extends JpaRepository<Buy, String> {

    Optional<Buy> findByBuyCode(String buyCode);

    @Query("select count(b) from Buy b where b.buyCode = :code and b.companyCode = :cc")
    long countByCodeAndCompany(@Param("code") String buyCode,
                               @Param("cc") String companyCode);
}
