package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.EstimateDetail;
import com.yedam.sales1.domain.InvoiceDetail;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, String> {

	List<InvoiceDetail> findAll();

	InvoiceDetail findByInvoiceUniqueCode(Long invoiceUniqueCode);

	@Query("SELECT MAX(i.invoiceDetailCode) FROM InvoiceDetail i")
	String findMaxInvoiceDetailCode();
}