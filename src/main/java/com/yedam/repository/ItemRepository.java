package com.yedam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yedam.domain.Item;

public interface ItemRepository extends
		JpaRepository<Item, String>{

}
