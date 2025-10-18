package com.yedam.sales1.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.InvoiceDetail;
import com.yedam.sales1.repository.InvoiceDetailRepository;
import com.yedam.sales1.service.InvoiceDetailService;

@Service
public class InvoiceDetailServiceImpl implements InvoiceDetailService {

	private final InvoiceDetailRepository invoiceDetailRepository;

	@Autowired
	public InvoiceDetailServiceImpl(InvoiceDetailRepository invoiceDetailRepository) {
		this.invoiceDetailRepository = invoiceDetailRepository;
	}

	@Override
	public List<InvoiceDetail> getAllInvoiceDetail() {
		return invoiceDetailRepository.findAll();
	}

	@Override
	public InvoiceDetail saveInvoiceDetail(InvoiceDetail invoiceDetail) {
		return invoiceDetailRepository.save(invoiceDetail);
	}



}
