package com.yedam.common.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.common.service.ScreenService;
import com.yedam.common.service.ScreenService.ModuleDto;
import com.yedam.common.service.ScreenService.ScreenDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/screens")
public class ScreenController {

    private final ScreenService screenService;

    /**
     * 모듈 드롭다운용
     * GET /api/screens/modules?onlyY=true
     */
    @GetMapping("/modules")
    public List<ModuleDto> modules(
            @RequestParam(name = "onlyY", defaultValue = "true") boolean onlyY
    ) {
        return screenService.findModules(onlyY);
    }

    /**
     * 화면 목록(권한 화면 우측 테이블 데이터)
     * 예) GET /api/screens?module=SALES&kw=관리&onlyY=true
     */
    @GetMapping
    public List<ScreenDto> list(
            @RequestParam(name = "module", required = false) String moduleCode,
            @RequestParam(name = "kw",      required = false) String keyword,
            @RequestParam(name = "onlyY",   defaultValue = "true") boolean onlyY
    ) {
        return screenService.findScreens(moduleCode, keyword, onlyY);
    }
}
