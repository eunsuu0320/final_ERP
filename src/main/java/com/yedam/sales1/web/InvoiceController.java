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

import com.yedam.sales1.domain.Invoice;
import com.yedam.sales1.service.InvoiceService;

@Controller
public class InvoiceController {

	
    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("invoiceList")
    public String invoiceList(Model model) {
        List<Invoice> invoice = invoiceService.getAllInvoice();
        
        Map<String, Object> tableData = invoiceService.getTableDataFromInvoice(invoice);

        model.addAttribute("columns", tableData.get("columns"));
        model.addAttribute("rows", tableData.get("rows"));

        return "sales1/invoiceList";
    }
    
    
    // 품목 등록
    @PostMapping("api/registInvoice")
    public ResponseEntity<Invoice> registOrders(@ModelAttribute Invoice invoices) {
    	Invoice saved = invoiceService.saveInvoice(invoices);
        return ResponseEntity.ok(saved);
    }
    
}
