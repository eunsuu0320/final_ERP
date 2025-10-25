package com.yedam.sales1.repository;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.InvoiceDetail;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, String> {

	// ✅ 기본 CRUD 제공하므로 findAll() 굳이 필요 없음 (삭제 가능)
	@Query("SELECT i FROM InvoiceDetail i where i.companyCode = :companyCode")
	List<InvoiceDetail> findAll(@Param("companyCode") String companyCode);

	// ✅ 특정 인보이스에 속한 디테일 전부 조회
	List<InvoiceDetail> findByInvoiceUniqueCode(Integer invoiceUniqueCode);

	// ✅ 마지막 코드 조회 (상세코드 자동생성용)
	@Query("SELECT MAX(i.invoiceDetailCode) FROM InvoiceDetail i")
	String findMaxInvoiceDetailCode();
}
