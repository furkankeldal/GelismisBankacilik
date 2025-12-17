package com.example.OnlineBankacilik.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
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

import com.example.OnlineBankacilik.dto.AccountRequestDto;
import com.example.OnlineBankacilik.dto.AccountResponseDto;
import com.example.OnlineBankacilik.dto.TransactionRequestDto;
import com.example.OnlineBankacilik.enums.AccountType;
import com.example.OnlineBankacilik.exception.AccountNotFoundException;
import com.example.OnlineBankacilik.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AccountController.class)
@DisplayName("Account Controller Integration Tests")
class AccountControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AccountService accountService;

	@Autowired
	private ObjectMapper objectMapper;

	private AccountRequestDto testAccountRequest;
	private AccountResponseDto testAccountResponse;
	private TransactionRequestDto testTransactionRequest;
	private LocalDateTime testDate;

	@BeforeEach
	void setUp() {
		testDate = LocalDateTime.now();

		testAccountRequest = new AccountRequestDto();
		testAccountRequest.setCustomerId(1L);
		testAccountRequest.setAccountType(AccountType.VADESIZ);
		testAccountRequest.setFirstAmount(new BigDecimal("1000.00"));

		testAccountResponse = new AccountResponseDto();
		testAccountResponse.setAccountNo("1001");
		testAccountResponse.setCustomerId(1L);
		testAccountResponse.setAccountType(AccountType.VADESIZ);
		testAccountResponse.setAmount(new BigDecimal("1000.00"));
		testAccountResponse.setActive(true);
		testAccountResponse.setOpeningDate(testDate);

		testTransactionRequest = new TransactionRequestDto();
		testTransactionRequest.setAmount(new BigDecimal("500.00"));
		testTransactionRequest.setExplanation("Test transaction");
	}

	@Test
	@DisplayName("POST /accounts - Should create account successfully")
	void testOpenAccount_Success() throws Exception {
		// Given
		when(accountService.accountOpen(any(AccountRequestDto.class))).thenReturn(testAccountResponse);

		// When & Then
		mockMvc.perform(post("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testAccountRequest)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accountNo").value("1001"))
				.andExpect(jsonPath("$.customerId").value(1L))
				.andExpect(jsonPath("$.accountType").value("VADESIZ"))
				.andExpect(jsonPath("$.amount").value(1000.00));

		verify(accountService, times(1)).accountOpen(any(AccountRequestDto.class));
	}

	@Test
	@DisplayName("POST /accounts - Should return 400 for invalid request")
	void testOpenAccount_InvalidRequest() throws Exception {
		// Given
		AccountRequestDto invalidDto = new AccountRequestDto();
		invalidDto.setCustomerId(null); // Invalid: null customerId
		invalidDto.setAccountType(null); // Invalid: null accountType

		// When & Then
		mockMvc.perform(post("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidDto)))
				.andExpect(status().isBadRequest());

		verify(accountService, never()).accountOpen(any(AccountRequestDto.class));
	}

	@Test
	@DisplayName("GET /accounts - Should return all accounts")
	void testGetAllAccounts_Success() throws Exception {
		// Given
		AccountResponseDto account2 = new AccountResponseDto();
		account2.setAccountNo("1002");
		account2.setCustomerId(2L);
		account2.setAccountType(AccountType.VADELI);
		account2.setAmount(new BigDecimal("2000.00"));
		account2.setActive(true);
		account2.setOpeningDate(testDate);

		List<AccountResponseDto> accounts = Arrays.asList(testAccountResponse, account2);
		when(accountService.allAccounts()).thenReturn(accounts);

		// When & Then
		mockMvc.perform(get("/accounts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].accountNo").value("1001"))
				.andExpect(jsonPath("$[1].accountNo").value("1002"));

		verify(accountService, times(1)).allAccounts();
	}

	@Test
	@DisplayName("GET /accounts/{accountNo} - Should return account by account number")
	void testGetAccountByAccountNo_Success() throws Exception {
		// Given
		when(accountService.getAccount("1001")).thenReturn(testAccountResponse);

		// When & Then
		mockMvc.perform(get("/accounts/1001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountNo").value("1001"))
				.andExpect(jsonPath("$.customerId").value(1L));

		verify(accountService, times(1)).getAccount("1001");
	}

	@Test
	@DisplayName("GET /accounts/{accountNo} - Should return 404 when account not found")
	void testGetAccountByAccountNo_NotFound() throws Exception {
		// Given
		when(accountService.getAccount("9999")).thenThrow(new AccountNotFoundException("9999"));

		// When & Then
		mockMvc.perform(get("/accounts/9999"))
				.andExpect(status().isNotFound());

		verify(accountService, times(1)).getAccount("9999");
	}

	@Test
	@DisplayName("GET /accounts/customer/{customerId} - Should return customer accounts")
	void testGetCustomerAccounts_Success() throws Exception {
		// Given
		List<AccountResponseDto> accounts = Arrays.asList(testAccountResponse);
		when(accountService.customerAccounts(1L)).thenReturn(accounts);

		// When & Then
		mockMvc.perform(get("/accounts/customer/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].accountNo").value("1001"));

		verify(accountService, times(1)).customerAccounts(1L);
	}

	@Test
	@DisplayName("POST /accounts/{accountNo}/deposit - Should deposit money successfully")
	void testDeposit_Success() throws Exception {
		// Given
		AccountResponseDto updatedAccount = new AccountResponseDto();
		updatedAccount.setAccountNo("1001");
		updatedAccount.setCustomerId(1L);
		updatedAccount.setAmount(new BigDecimal("1500.00"));
		updatedAccount.setActive(true);
		updatedAccount.setOpeningDate(testDate);

		when(accountService.deposit(eq("1001"), any(TransactionRequestDto.class))).thenReturn(updatedAccount);

		// When & Then
		mockMvc.perform(post("/accounts/1001/deposit")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testTransactionRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.amount").value(1500.00));

		verify(accountService, times(1)).deposit(eq("1001"), any(TransactionRequestDto.class));
	}

	@Test
	@DisplayName("POST /accounts/{accountNo}/withdraw - Should withdraw money successfully")
	void testWithdraw_Success() throws Exception {
		// Given
		AccountResponseDto updatedAccount = new AccountResponseDto();
		updatedAccount.setAccountNo("1001");
		updatedAccount.setCustomerId(1L);
		updatedAccount.setAmount(new BigDecimal("500.00"));
		updatedAccount.setActive(true);
		updatedAccount.setOpeningDate(testDate);

		when(accountService.withdraw(eq("1001"), any(TransactionRequestDto.class))).thenReturn(updatedAccount);

		// When & Then
		mockMvc.perform(post("/accounts/1001/withdraw")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testTransactionRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.amount").value(500.00));

		verify(accountService, times(1)).withdraw(eq("1001"), any(TransactionRequestDto.class));
	}

	@Test
	@DisplayName("DELETE /accounts/{accountNo} - Should close account successfully")
	void testCloseAccount_Success() throws Exception {
		// Given
		doNothing().when(accountService).closeAccount("1001");

		// When & Then
		mockMvc.perform(delete("/accounts/1001"))
				.andExpect(status().isNoContent());

		verify(accountService, times(1)).closeAccount("1001");
	}
}

