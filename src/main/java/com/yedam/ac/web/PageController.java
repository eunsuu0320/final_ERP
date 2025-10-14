package com.yedam.ac.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
  @GetMapping("/income")
  public String incomePage() { return "ac/income"; } // templates/income.html
}
