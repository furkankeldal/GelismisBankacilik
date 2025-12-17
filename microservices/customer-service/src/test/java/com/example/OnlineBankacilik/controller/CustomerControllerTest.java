package com.example.OnlineBankacilik.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.OnlineBankacilik.dto.CustomerRequestDto;
import com.example.OnlineBankacilik.dto.CustomerResponseDto;
import com.example.OnlineBankacilik.exception.CustomerNotFoundException;
import com.example.OnlineBankacilik.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CustomerController.class)
@DisplayName("Customer Controller Integration Tests")
class CustomerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CustomerService customerService;

	@Autowired
	private ObjectMapper objectMapper;

	private CustomerRequestDto testRequestDto;
	private CustomerResponseDto testResponseDto;
	private LocalDateTime testDate;

	@BeforeEach
	void setUp() {
		testDate = LocalDateTime.now();

		testRequestDto = new CustomerRequestDto();
		testRequestDto.setNameSurname("Ali Veli");
		testRequestDto.setTcKimlikNo("12345678901");
		testRequestDto.setNumber("5551234567");
		testRequestDto.setEmail("ali.veli@example.com");

		testResponseDto = new CustomerResponseDto();
		testResponseDto.setCustomerId(1L);
		testResponseDto.setNameSurname("Ali Veli");
		testResponseDto.setTcKimlikNo("12345678901");
		testResponseDto.setNumber("5551234567");
		testResponseDto.setEmail("ali.veli@example.com");
		testResponseDto.setRegistrationDate(testDate);
	}

	@Test
	@DisplayName("POST /customers - Should create customer successfully")
	void testAddCustomer_Success() throws Exception {
		// Given
		when(customerService.add(any(CustomerRequestDto.class))).thenReturn(testResponseDto);

		// When & Then
		mockMvc.perform(post("/customers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testRequestDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.customerId").value(1L))
				.andExpect(jsonPath("$.nameSurname").value("Ali Veli"))
				.andExpect(jsonPath("$.email").value("ali.veli@example.com"));

		verify(customerService, times(1)).add(any(CustomerRequestDto.class));
	}

	@Test
	@DisplayName("POST /customers - Should return 400 for invalid request")
	void testAddCustomer_InvalidRequest() throws Exception {
		// Given
		CustomerRequestDto invalidDto = new CustomerRequestDto();
		invalidDto.setNameSurname(""); // Invalid: empty name
		invalidDto.setEmail("invalid-email"); // Invalid: not a valid email

		// When & Then
		mockMvc.perform(post("/customers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidDto)))
				.andExpect(status().isBadRequest());

		verify(customerService, never()).add(any(CustomerRequestDto.class));
	}

	@Test
	@DisplayName("GET /customers - Should return all customers")
	void testGetAllCustomers_Success() throws Exception {
		// Given
		CustomerResponseDto customer2 = new CustomerResponseDto();
		customer2.setCustomerId(2L);
		customer2.setNameSurname("Ayşe Yılmaz");
		customer2.setTcKimlikNo("98765432109");
		customer2.setNumber("5559876543");
		customer2.setEmail("ayse.yilmaz@example.com");
		customer2.setRegistrationDate(testDate);

		List<CustomerResponseDto> customers = Arrays.asList(testResponseDto, customer2);
		when(customerService.allList()).thenReturn(customers);

		// When & Then
		mockMvc.perform(get("/customers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].customerId").value(1L))
				.andExpect(jsonPath("$[1].customerId").value(2L));

		verify(customerService, times(1)).allList();
	}

	@Test
	@DisplayName("GET /customers/{customerId} - Should return customer by id")
	void testGetCustomerById_Success() throws Exception {
		// Given
		when(customerService.getById(1L)).thenReturn(testResponseDto);

		// When & Then
		mockMvc.perform(get("/customers/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId").value(1L))
				.andExpect(jsonPath("$.nameSurname").value("Ali Veli"));

		verify(customerService, times(1)).getById(1L);
	}

	@Test
	@DisplayName("GET /customers/{customerId} - Should return 404 when customer not found")
	void testGetCustomerById_NotFound() throws Exception {
		// Given
		when(customerService.getById(999L)).thenThrow(new CustomerNotFoundException(999L));

		// When & Then
		mockMvc.perform(get("/customers/999"))
				.andExpect(status().isNotFound());

		verify(customerService, times(1)).getById(999L);
	}

	@Test
	@DisplayName("PUT /customers/{customerId} - Should update customer successfully")
	void testUpdateCustomer_Success() throws Exception {
		// Given
		CustomerRequestDto updateDto = new CustomerRequestDto();
		updateDto.setNameSurname("Ali Veli Updated");
		updateDto.setTcKimlikNo("12345678901");
		updateDto.setNumber("5559998888");
		updateDto.setEmail("ali.veli.updated@example.com");

		CustomerResponseDto updatedResponse = new CustomerResponseDto();
		updatedResponse.setCustomerId(1L);
		updatedResponse.setNameSurname("Ali Veli Updated");
		updatedResponse.setTcKimlikNo("12345678901");
		updatedResponse.setNumber("5559998888");
		updatedResponse.setEmail("ali.veli.updated@example.com");
		updatedResponse.setRegistrationDate(testDate);

		when(customerService.update(eq(1L), any(CustomerRequestDto.class))).thenReturn(updatedResponse);

		// When & Then
		mockMvc.perform(put("/customers/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nameSurname").value("Ali Veli Updated"))
				.andExpect(jsonPath("$.email").value("ali.veli.updated@example.com"));

		verify(customerService, times(1)).update(eq(1L), any(CustomerRequestDto.class));
	}

	@Test
	@DisplayName("DELETE /customers/{customerId} - Should delete customer successfully")
	void testDeleteCustomer_Success() throws Exception {
		// Given
		doNothing().when(customerService).delete(1L);

		// When & Then
		mockMvc.perform(delete("/customers/1"))
				.andExpect(status().isNoContent());

		verify(customerService, times(1)).delete(1L);
	}

	@Test
	@DisplayName("DELETE /customers/{customerId} - Should return 404 when customer not found")
	void testDeleteCustomer_NotFound() throws Exception {
		// Given
		doThrow(new CustomerNotFoundException(999L)).when(customerService).delete(999L);

		// When & Then
		mockMvc.perform(delete("/customers/999"))
				.andExpect(status().isNotFound());

		verify(customerService, times(1)).delete(999L);
	}
}

