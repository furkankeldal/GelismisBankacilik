package com.example.OnlineBankacilik.service;

import java.util.List;

import com.example.OnlineBankacilik.dto.CustomerRequestDto;
import com.example.OnlineBankacilik.dto.CustomerResponseDto;

public interface CustomerService {

	CustomerResponseDto add(CustomerRequestDto dto);

	List<CustomerResponseDto> allList();

	CustomerResponseDto getById(Long id);

	CustomerResponseDto update(Long id, CustomerRequestDto dto);

	void delete(Long id);
}
