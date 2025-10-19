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

    // ✅ 상세 조회
    List<OrderDetail> findByOrderUniqueCode(Long orderUniqueCode);

    // ✅ 품목명 요약용
    @Query("""
        SELECT DISTINCT p.productName
        FROM OrderDetail d
        JOIN Product p ON d.productCode = p.productCode
        WHERE d.orderUniqueCode = :orderUniqueCode
    """)
    List<String> findProductNamesByOrderUniqueCode(Long orderUniqueCode);
    

}
