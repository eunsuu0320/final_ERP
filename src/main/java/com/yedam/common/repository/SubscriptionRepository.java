package com.yedam.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.common.domain.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
	
	@Query("SELECT u FROM Subscription u WHERE u.companyCode = :companyCode")
	List<Subscription> findByCompanyCode(@Param("companyCode") String companyCode);
}
