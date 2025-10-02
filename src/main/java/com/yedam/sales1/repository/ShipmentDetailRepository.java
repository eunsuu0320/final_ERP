package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.ShipmentDetail;

@Repository
public interface ShipmentDetailRepository extends
		JpaRepository<ShipmentDetail, String>{

	// JpaRepository에 이미 정의되어 있으므로 생략 가능합니다.
	List<ShipmentDetail> findAll();

	// ⭐ [수정] ShipmentCode의 타입이 String이므로 Long 대신 String을 사용합니다.
	// 이 메서드는 단일 ShipmentCode에 해당하는 모든 상세 항목을 가져와야 하므로 List를 반환해야 합니다.
	List<ShipmentDetail> findByShipmentCode(String shipmentCode);

	@Query("SELECT MAX(sd.shipmentDetailCode) FROM ShipmentDetail sd")
	String findMaxShipmentDetailCode();

}