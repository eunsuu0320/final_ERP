package com.yedam.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	
	@GetMapping("index")
	public String index(Model model) {
		return "index";
	}
	
	@GetMapping("main")
	public String main(Model model) {
		return "page/main";
	}
}
