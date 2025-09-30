package com.yedam.sales1.dto;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Loan;
import com.yedam.sales1.domain.Payment;

import java.util.List;
import lombok.Data;

@Data
public class PartnerRegistrationDTO {
    private Partner partnerData;   
    private Loan loanPriceData;    
    private List<Payment> paymentData; 
}