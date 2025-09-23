package com.yedam.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SalesplanController {

	@GetMapping("salesList")
	public String salesList(Model model) {
		return "sales2/salesList";
	}
}
