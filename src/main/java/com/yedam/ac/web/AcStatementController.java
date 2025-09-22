package com.yedam.ac.web;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yedam.ac.service.StatementQueryService;
import com.yedam.ac.web.dto.StatementSearchForm;
import com.yedam.ac.web.dto.UnifiedStatementRow;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/ac")
@RequiredArgsConstructor
public class AcStatementController {

    private final StatementQueryService queryService;

    @GetMapping("/statements")
    public String list(@ModelAttribute("form") StatementSearchForm form, Model model) {
        Page<UnifiedStatementRow> page = queryService.search(form);
        model.addAttribute("page", page);
        return "ac/statement-list";
    }
}
