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

import com.yedam.sales1.domain.Orders;
import com.yedam.sales1.service.OrdersService;

@Controller
public class OrdersController {

	
    private final OrdersService ordersService;

    @Autowired
    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @GetMapping("ordersList")
    public String ordersList(Model model) {
        List<Orders> orders = ordersService.getAllOrders();
        
        Map<String, Object> tableData = ordersService.getTableDataFromOrders(orders);

        model.addAttribute("columns", tableData.get("columns"));
        model.addAttribute("rows", tableData.get("rows"));

        return "sales1/ordersList";
    }
    
    
    // 품목 등록
    @PostMapping("api/registOrders")
    public ResponseEntity<Orders> registOrders(@ModelAttribute Orders orders) {
    	Orders saved = ordersService.saveOrders(orders);
        return ResponseEntity.ok(saved);
    }
    
}
