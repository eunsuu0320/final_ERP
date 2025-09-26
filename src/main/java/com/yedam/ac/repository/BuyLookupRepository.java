package com.yedam.ac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.domain.BuyListView;

public interface BuyLookupRepository extends JpaRepository<BuyListView, String> {

    @Query("""
      select b from BuyListView b
      where (:kw is null or :kw = ''
             or lower(b.buyCode) like lower(concat('%', :kw, '%'))
             or lower(b.partnerName) like lower(concat('%', :kw, '%'))
             or lower(b.productName) like lower(concat('%', :kw, '%')))
      order by b.purchaseDate desc, b.buyCode desc
    """)
    List<BuyListView> search(@Param("kw") String kw);
}
