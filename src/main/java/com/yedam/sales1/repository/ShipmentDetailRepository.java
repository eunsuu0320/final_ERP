package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.ShipmentDetail;

@Repository
public interface ShipmentDetailRepository extends JpaRepository<ShipmentDetail, String> {

    List<ShipmentDetail> findAll();

    List<ShipmentDetail> findByShipmentCode(String shipmentCode);

    @Query("SELECT MAX(sd.shipmentDetailCode) FROM ShipmentDetail sd")
    String findMaxShipmentDetailCode();
}
