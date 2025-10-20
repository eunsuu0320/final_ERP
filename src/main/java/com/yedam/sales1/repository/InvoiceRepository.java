package com.yedam.sales1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.domain.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

	List<Invoice> findAll();

	@Query("SELECT MAX(p.invoiceCode) FROM Invoice p")
	String findMaxInvoiceCode();

	@Query("SELECT i FROM Invoice i WHERE i.invoiceCode = :invoiceCode ")
	Optional<Invoice> findByInvoiceCode(@Param("invoiceCode") String invoiceCode);
	
	
	@Query("SELECT p FROM Invoice p ")
	List<Invoice> findByFilter(@Param("searchVo") Invoice searchVo);
	
	
	
}
