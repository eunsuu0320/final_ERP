package com.yedam.hr.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import ch.qos.logback.core.model.Model;

@Controller
public class OnOffListController {

	@GetMapping("/onOffListPage")
	public String getOnOffListPage(Model model) {
		return "hr/onOffList";
	}
}
