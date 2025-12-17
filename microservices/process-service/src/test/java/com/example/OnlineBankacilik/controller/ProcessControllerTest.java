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

import com.example.OnlineBankacilik.dto.ProcessRequestDto;
import com.example.OnlineBankacilik.dto.ProcessResponseDto;
import com.example.OnlineBankacilik.enums.TransactionType;
import com.example.OnlineBankacilik.exception.AccountNotFoundException;
import com.example.OnlineBankacilik.service.ProcessService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProcessController.class)
@DisplayName("Process Controller Integration Tests")
class ProcessControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProcessService processService;

	@Autowired
	private ObjectMapper objectMapper;

	private ProcessRequestDto testDepositRequest;
	private ProcessRequestDto testWithdrawRequest;
	private ProcessResponseDto testDepositResponse;
	private ProcessResponseDto testWithdrawResponse;
	private LocalDateTime testDate;

	@BeforeEach
	void setUp() {
		testDate = LocalDateTime.now();

		testDepositRequest = new ProcessRequestDto();
		testDepositRequest.setAccountNo("1001");
		testDepositRequest.setAmount(new BigDecimal("500.00"));
		testDepositRequest.setExplanation("Test deposit");

		testWithdrawRequest = new ProcessRequestDto();
		testWithdrawRequest.setAccountNo("1001");
		testWithdrawRequest.setAmount(new BigDecimal("200.00"));
		testWithdrawRequest.setExplanation("Test withdraw");

		testDepositResponse = new ProcessResponseDto();
		testDepositResponse.setCustomerId(1L);
		testDepositResponse.setNumberOfAccount("1001");
		testDepositResponse.setTransactionType(TransactionType.YATIRMA);
		testDepositResponse.setAmount(new BigDecimal("500.00"));
		testDepositResponse.setPreviousBalance(new BigDecimal("1000.00"));
		testDepositResponse.setNewBalance(new BigDecimal("1500.00"));
		testDepositResponse.setExplanation("Test deposit");
		testDepositResponse.setSuccesfull(true);
		testDepositResponse.setRegistrationDate(testDate);

		testWithdrawResponse = new ProcessResponseDto();
		testWithdrawResponse.setCustomerId(1L);
		testWithdrawResponse.setNumberOfAccount("1001");
		testWithdrawResponse.setTransactionType(TransactionType.CEKME);
		testWithdrawResponse.setAmount(new BigDecimal("200.00"));
		testWithdrawResponse.setPreviousBalance(new BigDecimal("1000.00"));
		testWithdrawResponse.setNewBalance(new BigDecimal("800.00"));
		testWithdrawResponse.setExplanation("Test withdraw");
		testWithdrawResponse.setSuccesfull(true);
		testWithdrawResponse.setRegistrationDate(testDate);
	}

	@Test
	@DisplayName("POST /processes/deposit-money - Should deposit money successfully")
	void testDepositMoney_Success() throws Exception {
		// Given
		when(processService.deposit(any(ProcessRequestDto.class))).thenReturn(testDepositResponse);

		// When & Then
		mockMvc.perform(post("/processes/deposit-money")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testDepositRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.transactionType").value("YATIRMA"))
				.andExpect(jsonPath("$.amount").value(500.00))
				.andExpect(jsonPath("$.newBalance").value(1500.00))
				.andExpect(jsonPath("$.succesfull").value(true));

		verify(processService, times(1)).deposit(any(ProcessRequestDto.class));
	}

	@Test
	@DisplayName("POST /processes/deposit-money - Should return 400 for invalid request")
	void testDepositMoney_InvalidRequest() throws Exception {
		// Given
		ProcessRequestDto invalidDto = new ProcessRequestDto();
		invalidDto.setAccountNo(""); // Invalid: empty accountNo
		invalidDto.setAmount(null); // Invalid: null amount

		// When & Then
		mockMvc.perform(post("/processes/deposit-money")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidDto)))
				.andExpect(status().isBadRequest());

		verify(processService, never()).deposit(any(ProcessRequestDto.class));
	}

	@Test
	@DisplayName("POST /processes/withdraw-money - Should withdraw money successfully")
	void testWithdrawMoney_Success() throws Exception {
		// Given
		when(processService.withdraw(any(ProcessRequestDto.class))).thenReturn(testWithdrawResponse);

		// When & Then
		mockMvc.perform(post("/processes/withdraw-money")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testWithdrawRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.transactionType").value("CEKME"))
				.andExpect(jsonPath("$.amount").value(200.00))
				.andExpect(jsonPath("$.newBalance").value(800.00))
				.andExpect(jsonPath("$.succesfull").value(true));

		verify(processService, times(1)).withdraw(any(ProcessRequestDto.class));
	}

	@Test
	@DisplayName("GET /processes/amount/{accountNo} - Should get account amount successfully")
	void testAmount_Success() throws Exception {
		// Given
		ProcessResponseDto amountResponse = new ProcessResponseDto();
		amountResponse.setCustomerId(1L);
		amountResponse.setNumberOfAccount("1001");
		amountResponse.setNewBalance(new BigDecimal("1000.00"));

		when(processService.amount("1001")).thenReturn(amountResponse);

		// When & Then
		mockMvc.perform(get("/processes/amount/1001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.numberOfAccount").value("1001"))
				.andExpect(jsonPath("$.newBalance").value(1000.00));

		verify(processService, times(1)).amount("1001");
	}

	@Test
	@DisplayName("GET /processes/amount/{accountNo} - Should return 404 when account not found")
	void testAmount_AccountNotFound() throws Exception {
		// Given
		when(processService.amount("9999")).thenThrow(new AccountNotFoundException("9999"));

		// When & Then
		mockMvc.perform(get("/processes/amount/9999"))
				.andExpect(status().isNotFound());

		verify(processService, times(1)).amount("9999");
	}

	@Test
	@DisplayName("GET /processes/account-history/{accountNo} - Should get account history successfully")
	void testAccountHistory_Success() throws Exception {
		// Given
		List<ProcessResponseDto> history = Arrays.asList(testDepositResponse, testWithdrawResponse);
		when(processService.accountHistory("1001")).thenReturn(history);

		// When & Then
		mockMvc.perform(get("/processes/account-history/1001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].transactionType").value("YATIRMA"))
				.andExpect(jsonPath("$[1].transactionType").value("CEKME"));

		verify(processService, times(1)).accountHistory("1001");
	}
}

