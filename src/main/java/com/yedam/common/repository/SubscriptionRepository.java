package com.yedam.common.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.common.domain.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
	
	@Query("SELECT u FROM Subscription u WHERE u.companyCode = :companyCode")
	List<Subscription> findByCompanyCode(@Param("companyCode") String companyCode);
	
	// ACTIVE 구독 중 종료일이 가장 늦은 1건 (오라클 호환: ROWNUM=1)
    @Query(value = """
            SELECT *
              FROM (
                    SELECT s.*
                      FROM SUBSCRIPTION s
                     WHERE s.COMPANY_CODE = :companyCode
                       AND s.STATUS = :status
                     ORDER BY s.SUBSCRIPTION_END_DATE DESC
                   )
             WHERE ROWNUM = 1
            """, nativeQuery = true)
    Optional<Subscription> findLatestByCompanyCodeAndStatus(
            @Param("companyCode") String companyCode,
            @Param("status") String status
    );
}
