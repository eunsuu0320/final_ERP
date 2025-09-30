package com.yedam.hr.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import ch.qos.logback.core.model.Model;

@Controller
public class OnOffController {

	@GetMapping("/onOffPage")
	public String getOnOffPage(Model model) {
		return "hr/onOff";
	}
}
