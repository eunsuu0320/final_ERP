// src/main/java/com/yedam/sales3/web/SalesDashViewController.java
package com.yedam.sales3.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SalesDashViewController {

    @GetMapping("/salesdash")
    public String salesdash() {
        // templates/sales3/salesdash.html
        return "sales3/salesdash";
    }
}
