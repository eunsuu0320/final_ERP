package com.yedam.sales2.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.sales2.domain.DSalesPlan;
import com.yedam.sales2.domain.EmpPlan;
import com.yedam.sales2.domain.Sales;
import com.yedam.sales2.domain.SalesPlan;
import com.yedam.sales2.repository.DSalesPlanRepository;
import com.yedam.sales2.repository.SalesPlanRepository;
import com.yedam.sales2.repository.SalesRepository;
import com.yedam.sales2.service.SalesService;

@Service
public class SalesServiceImpl implements SalesService {

	@Autowired
	SalesRepository salesRepository;
	@Autowired
	SalesPlanRepository salesPlanRepository;
	@Autowired
	DSalesPlanRepository dsalesPlanRepository;

	@Override
	public List<Sales> findAll() {
		return salesRepository.findAll();
	}

	// 년도별 영업계획
	@Override
	public List<Map<String, Object>> findSalesStatsByYear() {
		return salesPlanRepository.findSalesStatsByYear();
	}

	@Override
	public List<Map<String, Object>> findLastYearSalesData() {
		return salesRepository.findLastYearSalesData();
	}

	@Override
	@Transactional
	public SalesPlan saveSalesPlan(SalesPlan salesplan) {

		// 메인 테이블 저장
		if (salesplan.getRegDate() == null) {
			salesplan.setRegDate(new Date());
		}
		SalesPlan savedMaster = salesPlanRepository.save(salesplan);

		// 세부 테이블 저장
		List<DSalesPlan> detailList = salesplan.getDetails(); 
		if (detailList != null && !detailList.isEmpty()) {
			for (DSalesPlan detail : detailList) {
				// FK 연결
				detail.setSalesPlan(savedMaster); // 객체로 세팅
			}
			dsalesPlanRepository.saveAll(detailList);
		}

		return savedMaster;
	}

	@Override
	public List<SalesPlan> getPlanByYear(int year) {
		return salesPlanRepository.findByPlanYear(year);
	}

	// 연도별 영업계획 상세조회
	@Override
	public List<DSalesPlan> getSalesPlanDetail(int year) {
		List<SalesPlan> plans = salesPlanRepository.findByPlanYear(year); 
		
		if (plans == null || plans.isEmpty()) {
			return Collections.emptyList();
		}
		
		SalesPlan masterPlan = plans.get(0);

		// 세부 계획 조회
		List<DSalesPlan> details = dsalesPlanRepository.findBySalesPlan(masterPlan);
		
		for (DSalesPlan detail : details) {
		    detail.setSalesPlanCode(masterPlan.getSalesPlanCode()); 
		}

		return details;
	}
	
	@Override
	@Transactional
	public String updateSalesPlanDetails(int planCode, List<DSalesPlan> updatedDetails) {
	    try {
	        SalesPlan master = salesPlanRepository.findById(planCode)
	                .orElseThrow(() -> new RuntimeException("해당 계획이 존재하지 않습니다."));

	        // 1. 기존 데이터 ID 추출
	        List<DSalesPlan> existingDetails = dsalesPlanRepository.findBySalesPlan(master);
	        Set<Integer> existingIds = existingDetails.stream()
	        		.map(DSalesPlan::getSalesPlanDetailCode)
	        		.collect(Collectors.toSet());

	        // 2. 전달된 데이터 ID 추출 (0 또는 null은 신규 레코드이므로 제외)
	        Set<Integer> updatedIds = updatedDetails.stream()
	        		.map(DSalesPlan::getSalesPlanDetailCode)
	        		.filter(id -> id != 0) 
	        		.collect(Collectors.toSet());

	        // 3. 삭제 대상 ID 찾기: DB에는 있지만, 전달된 리스트에는 없는 항목 (사용자가 삭제한 행)
	        List<Integer> idsToDelete = existingIds.stream()
	        		.filter(id -> !updatedIds.contains(id))
	        		.collect(Collectors.toList());

	        // 4. 삭제 실행
	        if (!idsToDelete.isEmpty()) {
	            dsalesPlanRepository.deleteAllById(idsToDelete); 
	        }

	        // 5. 업데이트 및 신규 등록 (Upsert) 실행
	        // DSalesPlan의 ID(salesPlanDetailCode)가 존재하면 업데이트, 0이거나 null이면 삽입(INSERT)을 수행합니다.
	        for (DSalesPlan detail : updatedDetails) {
	            detail.setSalesPlan(master);
	        }
	        dsalesPlanRepository.saveAll(updatedDetails);

	        return "success";
	    } catch (Exception e) {
	        throw new RuntimeException("데이터 업데이트 실패: " + e.getMessage());
	    }
	}
	
	// 📌 추가: 올해 영업계획 존재 여부 확인 (Controller 연동을 위해 필요)
    @Override
    public boolean checkSalesPlanExists(int year) {
        return salesPlanRepository.existsByPlanYear(year);
    }

    // 📌 추가: 신규 영업계획 등록 (Controller 연동을 위해 필요)
    @Override
    @Transactional
    public String insertSalesPlan(Authentication auth, List<DSalesPlan> detailList) {
    	String companyCode = auth.getName().split(":")[0];
    	String empCode = auth.getName().split(":")[2];
        if (detailList == null || detailList.isEmpty()) {
            throw new RuntimeException("세부 데이터가 없습니다.");
        }
        
        // 1. 마스터 데이터 생성 및 저장
        SalesPlan master = new SalesPlan();
        master.setPlanYear(new Date()); // 현재 연도로 설정됩니다.
        master.setRegDate(new Date());
        // TODO: 실제 사용자 코드 및 회사 코드로 변경해야 합니다.
        master.setEmpCode(empCode); 
        master.setCompanyCode(companyCode);
        SalesPlan savedMaster = salesPlanRepository.save(master);
        
        // 2. 세부 데이터에 마스터 연결 후 저장
        for (DSalesPlan detail : detailList) {
            detail.setSalesPlan(savedMaster);
        }
        dsalesPlanRepository.saveAll(detailList);
        dsalesPlanRepository.flush();
        
        // 영업사원 세부 등록
        salesPlanRepository.PR_EMP_PLAN(companyCode, "2025");
        return "success";
    }
    
    // 사원별 영업매출 목록
    @Override
	public List<Map<String, Object>> getEmpPlanList(String companyCode, String planYear) {
    	
    	List<Map<String, Object>> result = salesRepository.findEmpPlanLastYear(companyCode, planYear);

		return result;
	}
    
    // 미수금 to5
    @Override
    public List<Map<String, Object>> getTopOutstanding(String companyCode, int limit) {
        // 레포지토리 메서드명과 정확히 일치!
        return salesRepository.findTopOutstanding(companyCode, limit);
    }
    
}
