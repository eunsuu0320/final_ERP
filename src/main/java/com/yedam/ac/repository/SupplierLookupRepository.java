package com.yedam.ac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.web.dto.SupplierRow;

public interface SupplierLookupRepository extends JpaRepository<com.yedam.ac.domain.AcPartner, String> {
    @Query(value = """
        SELECT 
          s.SUPPLIER_CODE AS supplierCode,
          s.SUPPLIER_NAME AS supplierName,
          s.PHONE         AS phone,
          s.ADDRESS       AS address,
          s.REMARK        AS remark
        FROM SUPPLIERS s
        WHERE (:kw IS NULL OR :kw='' 
           OR LOWER(s.SUPPLIER_CODE) LIKE LOWER('%' || :kw || '%')
           OR LOWER(s.SUPPLIER_NAME) LIKE LOWER('%' || :kw || '%'))
        ORDER BY s.SUPPLIER_NAME
        """, nativeQuery = true)
    List<SupplierRow> searchSuppliers(@Param("kw") String kw);
}
