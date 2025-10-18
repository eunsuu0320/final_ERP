package com.yedam.sales1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Orders;

@Repository
public interface OrdersRepository extends
		JpaRepository<Orders, Long>{

	List<Orders> findAll();
	
	@Query("SELECT MAX(p.orderCode) FROM Orders p")
	String findMaxOrdersCode();
	
	@Query("SELECT o FROM Orders o WHERE o.orderCode = :orderCode ")
	Optional<Orders> findByOrderCode(@Param("orderCode") String orderCode);

}
