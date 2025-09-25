package com.yedam.sales1.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.yedam.sales1.domain.Shipment;
import com.yedam.sales1.service.ShipmentService;

@Controller
public class ShipmentController {

	
    private final ShipmentService shipmentService;

    @Autowired
    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("shipmentList")
    public String shipmentList(Model model) {
        List<Shipment> shipments = shipmentService.getAllShipment();
        
        Map<String, Object> tableData = shipmentService.getTableDataFromShipments(shipments);

        model.addAttribute("columns", tableData.get("columns"));
        model.addAttribute("rows", tableData.get("rows"));

        return "sales1/shipmentList";
    }
    
    
    // 품목 등록
    @PostMapping("api/registShipment")
    public ResponseEntity<Shipment> registShipment(@ModelAttribute Shipment shipment) {
    	Shipment saved = shipmentService.saveShipment(shipment);
        return ResponseEntity.ok(saved);
    }
    
}
