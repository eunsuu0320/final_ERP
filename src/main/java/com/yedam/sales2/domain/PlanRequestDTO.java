package com.yedam.sales2.domain;

import java.util.List;

public class PlanRequestDTO {
    private int empCode;
    private int spCode;
    private int companyCode;
    private List<EsdpPlan> detailPlans;  // VO 재활용

    // getter/setter
    public int getEmpCode() { return empCode; }
    public void setEmpCode(int empCode) { this.empCode = empCode; }

    public int getSpCode() { return spCode; }
    public void setSpCode(int spCode) { this.spCode = spCode; }

    public int getCompanyCode() { return companyCode; }
    public void setCompanyCode(int companyCode) { this.companyCode = companyCode; }

    public List<EsdpPlan> getDetailPlans() { return detailPlans; }
    public void setDetailPlans(List<EsdpPlan> detailPlans) { this.detailPlans = detailPlans; }
}
