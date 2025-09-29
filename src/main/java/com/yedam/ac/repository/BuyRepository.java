package com.yedam.ac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yedam.ac.domain.Buy;

public interface BuyRepository extends JpaRepository<Buy, String> {

    List<Buy> findTop50ByCompanyCodeAndBuyCodeContainingOrderByBuyCodeDesc(String companyCode, String keyword);

    List<Buy> findTop50ByCompanyCodeAndPartnerNameContainingOrderByBuyCodeDesc(String companyCode, String partnerName);
}
