package com.yedam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.domain.Product;

@Repository
public interface ProductRepository extends
		JpaRepository<Product, String>{

	@Query("SELECT p FROM Product p")
	List<Product> findAll();
}
