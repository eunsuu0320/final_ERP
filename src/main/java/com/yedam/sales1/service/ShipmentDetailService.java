package com.yedam.sales1.service;

import java.util.List;
import com.yedam.sales1.domain.ShipmentDetail;

public interface ShipmentDetailService {
    List<ShipmentDetail> getAllOrderDetail();

    ShipmentDetail saveShipmentDetail(ShipmentDetail shipmentDetail);
}
