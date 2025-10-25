package com.yedam.sales1.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.domain.Orders;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
	
	@Query("SELECT od FROM Orders od where od.companyCode = :companyCode")
	List<Orders> findAll(@Param("companyCode") String companyCode);

	@Query("SELECT MAX(p.orderCode) FROM Orders p")
	String findMaxOrdersCode();

	@Query("SELECT o FROM Orders o WHERE o.orderCode = :orderCode")
	Optional<Orders> findByOrderCode(String orderCode);

	@Query("SELECT p FROM Orders p ")
	List<Orders> findByFilter(Orders searchVo);

	@Query("""
			    select e from Orders e
			    where e.companyCode = :companyCode
			    and e.status not in('출하지시완료','미확인')
			    and e.partnerCode = :partnerCode
			""")
	List<Orders> findByCompanyCode(@Param("companyCode") String companyCode,
			@Param("partnerCode") String partnerCode);
}
