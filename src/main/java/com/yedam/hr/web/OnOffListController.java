package com.yedam.hr.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OnOffListController {

	@GetMapping("/onOffListPage")
	public String getOnOffListPage(Model model) {
		return "hr/onOffList";
	}
}
