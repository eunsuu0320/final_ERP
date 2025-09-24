// com.yedam.ac.repository.PartnerRepository.java
package com.yedam.ac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yedam.ac.domain.Partner;

public interface PartnerRepository extends JpaRepository<Partner, String> {

    List<Partner> findTop200ByOrderByPartnerNameAsc();

    List<Partner> findTop200ByPartnerNameContainingIgnoreCaseOrPartnerCodeContainingIgnoreCaseOrPartnerPhoneContainingIgnoreCaseOrManagerContainingIgnoreCaseOrderByPartnerNameAsc(
            String q1, String q2, String q3, String q4);
}