package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.OrderDetail;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, String> {

	List<OrderDetail> findAll();

	OrderDetail findByOrderDetailCode(String orderDetailCode);

	@Query("SELECT MAX(od.orderDetailCode) FROM OrderDetail od")
	String findMaxOrderDetailCode();

}
