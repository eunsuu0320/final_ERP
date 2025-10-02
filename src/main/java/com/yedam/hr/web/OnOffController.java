package com.yedam.hr.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OnOffController {

	@GetMapping("/onOffPage")
	public String getOnOffPage(Model model) {
		return "hr/onOff";
	}
}
