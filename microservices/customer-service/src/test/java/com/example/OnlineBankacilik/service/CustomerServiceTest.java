package com.example.OnlineBankacilik.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.OnlineBankacilik.dto.CustomerRequestDto;
import com.example.OnlineBankacilik.dto.CustomerResponseDto;
import com.example.OnlineBankacilik.entity.Customer;
import com.example.OnlineBankacilik.exception.CustomerNotFoundException;
import com.example.OnlineBankacilik.repository.CustomerRepository;
import com.example.OnlineBankacilik.service.impl.CustomerServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Unit Tests")
class CustomerServiceTest {

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private CustomerServiceImpl customerService;

	private Customer testCustomer;
	private CustomerRequestDto testRequestDto;
	private LocalDateTime testDate;

	@BeforeEach
	void setUp() {
		testDate = LocalDateTime.now();
		
		testCustomer = new Customer();
		testCustomer.setId(1L);
		testCustomer.setNameSurname("Ali Veli");
		testCustomer.setTcKimlikNo("12345678901");
		testCustomer.setNumber("5551234567");
		testCustomer.setEmail("ali.veli@example.com");
		testCustomer.setRegistrationDate(testDate);

		testRequestDto = new CustomerRequestDto();
		testRequestDto.setNameSurname("Ali Veli");
		testRequestDto.setTcKimlikNo("12345678901");
		testRequestDto.setNumber("5551234567");
		testRequestDto.setEmail("ali.veli@example.com");
	}

	@Test
	@DisplayName("Should add customer successfully")
	void testAddCustomer_Success() {
		// Given
		Customer savedCustomer = new Customer();
		savedCustomer.setId(1L);
		savedCustomer.setNameSurname(testRequestDto.getNameSurname());
		savedCustomer.setTcKimlikNo(testRequestDto.getTcKimlikNo());
		savedCustomer.setNumber(testRequestDto.getNumber());
		savedCustomer.setEmail(testRequestDto.getEmail());
		savedCustomer.setRegistrationDate(testDate);

		when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

		// When
		CustomerResponseDto result = customerService.add(testRequestDto);

		// Then
		assertNotNull(result);
		assertEquals(1L, result.getCustomerId());
		assertEquals("Ali Veli", result.getNameSurname());
		assertEquals("12345678901", result.getTcKimlikNo());
		assertEquals("5551234567", result.getNumber());
		assertEquals("ali.veli@example.com", result.getEmail());
		verify(customerRepository, times(1)).save(any(Customer.class));
	}

	@Test
	@DisplayName("Should return all customers")
	void testAllList_Success() {
		// Given
		Customer customer1 = new Customer();
		customer1.setId(1L);
		customer1.setNameSurname("Ali Veli");
		customer1.setTcKimlikNo("12345678901");
		customer1.setNumber("5551234567");
		customer1.setEmail("ali.veli@example.com");
		customer1.setRegistrationDate(testDate);

		Customer customer2 = new Customer();
		customer2.setId(2L);
		customer2.setNameSurname("Ayşe Yılmaz");
		customer2.setTcKimlikNo("98765432109");
		customer2.setNumber("5559876543");
		customer2.setEmail("ayse.yilmaz@example.com");
		customer2.setRegistrationDate(testDate);

		List<Customer> customers = Arrays.asList(customer1, customer2);
		when(customerRepository.findAll()).thenReturn(customers);

		// When
		List<CustomerResponseDto> result = customerService.allList();

		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("Ali Veli", result.get(0).getNameSurname());
		assertEquals("Ayşe Yılmaz", result.get(1).getNameSurname());
		verify(customerRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("Should throw exception when customer list is empty")
	void testAllList_EmptyList_ThrowsException() {
		// Given
		when(customerRepository.findAll()).thenReturn(List.of());

		// When & Then
		assertThrows(CustomerNotFoundException.class, () -> customerService.allList());
		verify(customerRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("Should get customer by id successfully")
	void testGetById_Success() {
		// Given
		when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

		// When
		CustomerResponseDto result = customerService.getById(1L);

		// Then
		assertNotNull(result);
		assertEquals(1L, result.getCustomerId());
		assertEquals("Ali Veli", result.getNameSurname());
		verify(customerRepository, times(1)).findById(1L);
	}

	@Test
	@DisplayName("Should throw exception when customer not found by id")
	void testGetById_NotFound_ThrowsException() {
		// Given
		when(customerRepository.findById(999L)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(CustomerNotFoundException.class, () -> customerService.getById(999L));
		verify(customerRepository, times(1)).findById(999L);
	}

	@Test
	@DisplayName("Should update customer successfully")
	void testUpdate_Success() {
		// Given
		CustomerRequestDto updateDto = new CustomerRequestDto();
		updateDto.setNameSurname("Ali Veli Updated");
		updateDto.setTcKimlikNo("12345678901");
		updateDto.setNumber("5559998888");
		updateDto.setEmail("ali.veli.updated@example.com");

		Customer updatedCustomer = new Customer();
		updatedCustomer.setId(1L);
		updatedCustomer.setNameSurname("Ali Veli Updated");
		updatedCustomer.setTcKimlikNo("12345678901");
		updatedCustomer.setNumber("5559998888");
		updatedCustomer.setEmail("ali.veli.updated@example.com");
		updatedCustomer.setRegistrationDate(testDate);

		when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
		when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

		// When
		CustomerResponseDto result = customerService.update(1L, updateDto);

		// Then
		assertNotNull(result);
		assertEquals(1L, result.getCustomerId());
		assertEquals("Ali Veli Updated", result.getNameSurname());
		assertEquals("5559998888", result.getNumber());
		assertEquals("ali.veli.updated@example.com", result.getEmail());
		verify(customerRepository, times(1)).findById(1L);
		verify(customerRepository, times(1)).save(any(Customer.class));
	}

	@Test
	@DisplayName("Should throw exception when updating non-existent customer")
	void testUpdate_NotFound_ThrowsException() {
		// Given
		when(customerRepository.findById(999L)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(CustomerNotFoundException.class, () -> customerService.update(999L, testRequestDto));
		verify(customerRepository, times(1)).findById(999L);
		verify(customerRepository, never()).save(any(Customer.class));
	}

	@Test
	@DisplayName("Should delete customer successfully")
	void testDelete_Success() {
		// Given
		when(customerRepository.existsById(1L)).thenReturn(true);
		doNothing().when(customerRepository).deleteById(1L);

		// When
		customerService.delete(1L);

		// Then
		verify(customerRepository, times(1)).existsById(1L);
		verify(customerRepository, times(1)).deleteById(1L);
	}

	@Test
	@DisplayName("Should throw exception when deleting non-existent customer")
	void testDelete_NotFound_ThrowsException() {
		// Given
		when(customerRepository.existsById(999L)).thenReturn(false);

		// When & Then
		assertThrows(CustomerNotFoundException.class, () -> customerService.delete(999L));
		verify(customerRepository, times(1)).existsById(999L);
		verify(customerRepository, never()).deleteById(anyLong());
	}
}

