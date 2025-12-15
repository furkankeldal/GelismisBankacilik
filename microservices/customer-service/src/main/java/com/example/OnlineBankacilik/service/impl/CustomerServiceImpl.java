package com.example.OnlineBankacilik.service.impl;

import java.util.List;


import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import com.example.OnlineBankacilik.dto.CustomerRequestDto;
import com.example.OnlineBankacilik.dto.CustomerResponseDto;
import com.example.OnlineBankacilik.entity.Customer;
import com.example.OnlineBankacilik.exception.CustomerNotFoundException;
import com.example.OnlineBankacilik.repository.CustomerRepository;
import com.example.OnlineBankacilik.service.CustomerService;


@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

	
	private final CustomerRepository customerRepository;

	private CustomerResponseDto toDto(Customer c) {
		CustomerResponseDto crd = new CustomerResponseDto();
		crd.setCustomerId(c.getId());
		crd.setNameSurname(c.getNameSurname());
		crd.setNumber(c.getNumber());
		crd.setEmail(c.getEmail());
		crd.setTcKimlikNo(c.getTcKimlikNo());
		crd.setRegistrationDate(c.getRegistrationDate());

		return crd;
	}

	@Override
	@CacheEvict(value = "customers", allEntries = true) // liste cache'ini boz
	public CustomerResponseDto add(CustomerRequestDto dto) {
		Customer customer = new Customer();
		customer.setNameSurname(dto.getNameSurname());
		customer.setTcKimlikNo(dto.getTcKimlikNo());
		customer.setEmail(dto.getEmail());
		customer.setNumber(dto.getNumber());

		return toDto(customerRepository.save(customer));
	}

	@Override
	@Cacheable(value = "customers") // tüm listeyi cache'le
	public List<CustomerResponseDto> allList() {
		System.out.println(">>> allList() METODU ÇALIŞTI – DB'YE GİDİYORUM");
		List<Customer> customers = customerRepository.findAll();
		if (customers.isEmpty()) {
			throw new CustomerNotFoundException("kullanıcı bulunamadı");
		}
		return customers.stream().map(this::toDto).toList();
	}

	@Override
	@Cacheable(value = "customerById", key = "#id") // id'ye göre cache'le
	public CustomerResponseDto getById(Long id) {
		return customerRepository.findById(id).map(this::toDto).orElseThrow(() -> new CustomerNotFoundException(id));
	}

	@Override
	@CacheEvict(value = { "customers", "customerById" }, key = "#id", allEntries = true)
	public CustomerResponseDto update(Long id, CustomerRequestDto dto) {
		Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
		customer.setNameSurname(dto.getNameSurname());
		customer.setEmail(dto.getEmail());
		customer.setNumber(dto.getNumber());
		customer.setTcKimlikNo(dto.getTcKimlikNo());

		return toDto(customerRepository.save(customer));
	}

	@Override
	@CacheEvict(value = { "customers", "customerById" }, key = "#id", allEntries = true)
	public void delete(Long id) {
		if (!customerRepository.existsById(id))
			throw new CustomerNotFoundException(id);
		customerRepository.deleteById(id);
	}

}

