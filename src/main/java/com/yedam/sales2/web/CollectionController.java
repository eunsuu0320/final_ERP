package com.yedam.sales2.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * 수금관리
 */

@Controller
public class CollectionController {
	
	// 수금관리 HTML
	@GetMapping("collection")
	public String collection() {
		return "sales2/collection";
	}
}
