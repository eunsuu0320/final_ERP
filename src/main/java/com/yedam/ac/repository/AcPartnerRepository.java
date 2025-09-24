// com.yedam.ac.repository.PartnerRepository.java
package com.yedam.ac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yedam.ac.domain.AcPartner;

public interface AcPartnerRepository extends JpaRepository<AcPartner, String> {

    List<AcPartner> findTop200ByOrderByPartnerNameAsc();

    List<AcPartner> findTop200ByPartnerNameContainingIgnoreCaseOrPartnerCodeContainingIgnoreCaseOrPartnerPhoneContainingIgnoreCaseOrManagerContainingIgnoreCaseOrderByPartnerNameAsc(
            String q1, String q2, String q3, String q4);
}