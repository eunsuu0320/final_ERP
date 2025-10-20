package com.yedam.sales2.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.sales2.repository.BusinessProfitsRepository;
import com.yedam.sales2.service.BusinessProfitsService;
import com.yedam.sales2.service.EmployeeProfitsService;

/*
 * 제품별, 사원별 영업이익조회
 */
@Controller
public class BusinessProfitsController {

    @Autowired
    private BusinessProfitsService businessProfitsService;

    @Autowired
    private EmployeeProfitsService employeeProfitsService;

    @Autowired
    private BusinessProfitsRepository repository;

    // 영업이익조회 HTML
    @GetMapping("businessProfits")
    public String businessProfits() {
        return "sales2/businessProfits";
    }

    // 사원별이익조회 HTML
    @GetMapping("employeeProfits")
    public String employeeProfits() {
        return "sales2/employeeProfits";
    }

    // 품목별 영업이익 조회 API
    @GetMapping("/api/sales/profit-list")
    @ResponseBody
    public List<Map<String, Object>> getSalesProfitList(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String quarter,
            @RequestParam(required = false) String keyword
    ) {
        return businessProfitsService.getSalesProfitList(year, quarter, keyword);
    }

    @ResponseBody
    @GetMapping("/partners")
    public List<Map<String, Object>> getEmpPartners(
            @RequestParam String companyCode,
            @RequestParam String empCode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) String keyword
    ) {
        return employeeProfitsService.getEmpPartners(companyCode, empCode, year, quarter, keyword);
    }

    // ✅ 차트: year만 있으면 해당 연도의 1~4분기, year+quarter면 그 분기를 끝으로 직전 4분기(포함)
    @GetMapping("/api/sales/profit-by-quarter-smart")
    @ResponseBody
    public List<Map<String, Object>> profitByQuarterSmart(
            @RequestParam String year,
            @RequestParam(required = false) String quarter,
            @RequestParam(required = false) String keyword
    ) {
        // 4개 (연,분기) 쌍 계산
        List<int[]> pairs = computeFourPairs(year, quarter);

        String y1 = String.valueOf(pairs.get(0)[0]); String q1 = String.valueOf(pairs.get(0)[1]);
        String y2 = String.valueOf(pairs.get(1)[0]); String q2 = String.valueOf(pairs.get(1)[1]);
        String y3 = String.valueOf(pairs.get(2)[0]); String q3 = String.valueOf(pairs.get(2)[1]);
        String y4 = String.valueOf(pairs.get(3)[0]); String q4 = String.valueOf(pairs.get(3)[1]);

        return repository.findProfitRateForFourPairs(y1, q1, y2, q2, y3, q3, y4, q4, keyword);
    }

    /**
     * quarter가 없으면: 해당 year의 1~4분기
     * quarter가 있으면: (year, quarter)을 끝으로 직전 4분기(선택분기 포함)
     *   ex) year=2024, quarter=2 -> (2023,3),(2023,4),(2024,1),(2024,2)
     */
    private List<int[]> computeFourPairs(String year, String quarter) {
        int y = Integer.parseInt(year);
        List<int[]> out = new ArrayList<>(4);

        if (quarter == null || quarter.isBlank()) {
            // 그 해 1~4분기
            out.add(new int[]{y, 1});
            out.add(new int[]{y, 2});
            out.add(new int[]{y, 3});
            out.add(new int[]{y, 4});
            return out;
        }

        int q = Integer.parseInt(quarter);
        // 분기 키: y*4 + q 를 기준으로 직전 3분기 + 현재 분기
        int startKey = y * 4 + q - 3; // 4개를 오름차순으로 반환
        for (int k = startKey; k <= y * 4 + q; k++) {
            int yy = Math.floorDiv(k, 4);
            int qq = k % 4;
            if (qq == 0) { qq = 4; yy -= 1; }
            out.add(new int[]{yy, qq});
        }
        return out;
    }
}
