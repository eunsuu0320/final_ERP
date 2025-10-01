// src/main/java/com/yedam/ac/web/AcReciptPageController.java
package com.yedam.ac.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AcReciptPageController {

    @GetMapping("/acrecipt")
    public String acreciptPage() {
        return "ac/acrecipt"; // templates/ac/acrecipt.html
    }
}
