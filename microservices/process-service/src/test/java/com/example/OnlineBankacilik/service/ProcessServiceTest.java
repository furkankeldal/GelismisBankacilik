package com.example.OnlineBankacilik.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.OnlineBankacilik.Kafka.TransactionProducer;
import com.example.OnlineBankacilik.client.AccountServiceClient;
import com.example.OnlineBankacilik.dto.AccountResponseDto;
import com.example.OnlineBankacilik.dto.ProcessRequestDto;
import com.example.OnlineBankacilik.dto.ProcessResponseDto;
import com.example.OnlineBankacilik.dto.TransactionRequestDto;
import com.example.OnlineBankacilik.entity.Process;
import com.example.OnlineBankacilik.enums.AccountType;
import com.example.OnlineBankacilik.enums.TransactionType;
import com.example.OnlineBankacilik.exception.AccountNotFoundException;
import com.example.OnlineBankacilik.exception.InsufficientBalanceException;
import com.example.OnlineBankacilik.repository.ProcessRepository;
import com.example.OnlineBankacilik.service.impl.ProcessServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Process Service Unit Tests")
class ProcessServiceTest {

	@Mock
	private ProcessRepository processRepository;

	@Mock
	private AccountServiceClient accountServiceClient;

	@Mock
	private TransactionProducer transactionProducer;

	@InjectMocks
	private ProcessServiceImpl processService;

	private AccountResponseDto testAccount;
	private ProcessRequestDto testDepositRequest;
	private ProcessRequestDto testWithdrawRequest;
	private Process testProcess;
	private LocalDateTime testDate;

	@BeforeEach
	void setUp() {
		testDate = LocalDateTime.now();

		testAccount = new AccountResponseDto();
		testAccount.setAccountNo("1001");
		testAccount.setCustomerId(1L);
		testAccount.setAccountType(AccountType.VADESIZ);
		testAccount.setAmount(new BigDecimal("1000.00"));
		testAccount.setActive(true);

		testDepositRequest = new ProcessRequestDto();
		testDepositRequest.setAccountNo("1001");
		testDepositRequest.setAmount(new BigDecimal("500.00"));
		testDepositRequest.setExplanation("Test deposit");

		testWithdrawRequest = new ProcessRequestDto();
		testWithdrawRequest.setAccountNo("1001");
		testWithdrawRequest.setAmount(new BigDecimal("200.00"));
		testWithdrawRequest.setExplanation("Test withdraw");

		testProcess = new Process();
		testProcess.setTransactionCode("TXN-001");
		testProcess.setAccountNo("1001");
		testProcess.setTransactionType(TransactionType.YATIRMA);
		testProcess.setAmount(new BigDecimal("500.00"));
		testProcess.setPreviousBalance(new BigDecimal("1000.00"));
		testProcess.setNewBalance(new BigDecimal("1500.00"));
		testProcess.setExplanation("Test deposit");
		testProcess.setSuccessful(true);
		testProcess.setTransactionDate(testDate);
	}

	@Test
	@DisplayName("Should deposit money successfully")
	void testDeposit_Success() {
		// Given
		when(accountServiceClient.getAccount("1001")).thenReturn(testAccount);
		when(processRepository.count()).thenReturn(0L);
		when(processRepository.save(any(Process.class))).thenReturn(testProcess);
		when(accountServiceClient.deposit(eq("1001"), any(TransactionRequestDto.class)))
				.thenReturn(testAccount);
		doNothing().when(transactionProducer).publish(any());

		// When
		ProcessResponseDto result = processService.deposit(testDepositRequest);

		// Then
		assertNotNull(result);
		assertEquals(1L, result.getCustomerId());
		assertEquals("1001", result.getNumberOfAccount());
		assertEquals(TransactionType.YATIRMA, result.getTransactionType());
		assertEquals(new BigDecimal("500.00"), result.getAmount());
		assertTrue(result.isSuccesfull());
		verify(accountServiceClient, times(1)).getAccount("1001");
		verify(accountServiceClient, times(1)).deposit(eq("1001"), any(TransactionRequestDto.class));
		verify(processRepository, times(1)).save(any(Process.class));
		verify(transactionProducer, times(1)).publish(any());
	}

	@Test
	@DisplayName("Should throw exception when account not found for deposit")
	void testDeposit_AccountNotFound_ThrowsException() {
		// Given
		when(accountServiceClient.getAccount("9999")).thenReturn(null);

		// When & Then
		assertThrows(AccountNotFoundException.class, () -> processService.deposit(testDepositRequest));
		verify(accountServiceClient, times(1)).getAccount("1001");
		verify(accountServiceClient, never()).deposit(anyString(), any(TransactionRequestDto.class));
		verify(processRepository, never()).save(any(Process.class));
	}

	@Test
	@DisplayName("Should withdraw money successfully")
	void testWithdraw_Success() {
		// Given
		Process withdrawProcess = new Process();
		withdrawProcess.setTransactionCode("TXN-002");
		withdrawProcess.setAccountNo("1001");
		withdrawProcess.setTransactionType(TransactionType.CEKME);
		withdrawProcess.setAmount(new BigDecimal("200.00"));
		withdrawProcess.setPreviousBalance(new BigDecimal("1000.00"));
		withdrawProcess.setNewBalance(new BigDecimal("800.00"));
		withdrawProcess.setExplanation("Test withdraw");
		withdrawProcess.setSuccessful(true);
		withdrawProcess.setTransactionDate(testDate);

		AccountResponseDto updatedAccount = new AccountResponseDto();
		updatedAccount.setAccountNo("1001");
		updatedAccount.setCustomerId(1L);
		updatedAccount.setAmount(new BigDecimal("800.00"));

		when(accountServiceClient.getAccount("1001")).thenReturn(testAccount);
		when(processRepository.count()).thenReturn(1L);
		when(processRepository.save(any(Process.class))).thenReturn(withdrawProcess);
		when(accountServiceClient.withdraw(eq("1001"), any(TransactionRequestDto.class)))
				.thenReturn(updatedAccount);
		doNothing().when(transactionProducer).publish(any());

		// When
		ProcessResponseDto result = processService.withdraw(testWithdrawRequest);

		// Then
		assertNotNull(result);
		assertEquals(TransactionType.CEKME, result.getTransactionType());
		assertEquals(new BigDecimal("200.00"), result.getAmount());
		assertEquals(new BigDecimal("800.00"), result.getNewBalance());
		assertTrue(result.isSuccesfull());
		verify(accountServiceClient, times(1)).getAccount("1001");
		verify(accountServiceClient, times(1)).withdraw(eq("1001"), any(TransactionRequestDto.class));
		verify(processRepository, times(1)).save(any(Process.class));
	}

	@Test
	@DisplayName("Should throw exception when insufficient balance for withdraw")
	void testWithdraw_InsufficientBalance_ThrowsException() {
		// Given
		ProcessRequestDto largeWithdrawRequest = new ProcessRequestDto();
		largeWithdrawRequest.setAccountNo("1001");
		largeWithdrawRequest.setAmount(new BigDecimal("2000.00"));

		when(accountServiceClient.getAccount("1001")).thenReturn(testAccount);
		// ProcessServiceImpl.withdraw() metodunda bakiye kontrolü yapılıyor ve exception fırlatılıyor
		// Bu yüzden accountServiceClient.withdraw() çağrılmıyor

		// When & Then
		// ProcessServiceImpl'de bakiye kontrolü yapılıyor: former.compareTo(dto.getAmount()) < 0
		// Bu durumda InsufficientBalanceException fırlatılıyor
		assertThrows(InsufficientBalanceException.class, () -> processService.withdraw(largeWithdrawRequest));
		verify(accountServiceClient, times(1)).getAccount("1001");
		// withdraw() çağrılmıyor çünkü bakiye kontrolü önce yapılıyor ve exception fırlatılıyor
		verify(accountServiceClient, never()).withdraw(anyString(), any(TransactionRequestDto.class));
	}

	@Test
	@DisplayName("Should get account amount successfully")
	void testAmount_Success() {
		// Given
		when(accountServiceClient.getAccount("1001")).thenReturn(testAccount);

		// When
		ProcessResponseDto result = processService.amount("1001");

		// Then
		assertNotNull(result);
		// amount() metodu customerId set etmiyor, sadece numberOfAccount, newBalance ve transactionType set ediyor
		assertEquals("1001", result.getNumberOfAccount());
		assertEquals(new BigDecimal("1000.00"), result.getNewBalance());
		assertEquals(TransactionType.YATIRMA, result.getTransactionType());
		verify(accountServiceClient, times(1)).getAccount("1001");
	}

	@Test
	@DisplayName("Should throw exception when account not found for amount")
	void testAmount_AccountNotFound_ThrowsException() {
		// Given
		when(accountServiceClient.getAccount("9999")).thenReturn(null);

		// When & Then
		assertThrows(AccountNotFoundException.class, () -> processService.amount("9999"));
		verify(accountServiceClient, times(1)).getAccount("9999");
	}

	@Test
	@DisplayName("Should get account history successfully")
	void testAccountHistory_Success() {
		// Given
		Process process1 = new Process();
		process1.setTransactionCode("TXN-001");
		process1.setAccountNo("1001");
		process1.setTransactionType(TransactionType.YATIRMA);
		process1.setAmount(new BigDecimal("500.00"));
		process1.setTransactionDate(testDate);

		Process process2 = new Process();
		process2.setTransactionCode("TXN-002");
		process2.setAccountNo("1001");
		process2.setTransactionType(TransactionType.CEKME);
		process2.setAmount(new BigDecimal("200.00"));
		process2.setTransactionDate(testDate);

		List<Process> processes = Arrays.asList(process1, process2);
		when(processRepository.findByAccountNo("1001")).thenReturn(processes);
		when(accountServiceClient.getAccount("1001")).thenReturn(testAccount);

		// When
		List<ProcessResponseDto> result = processService.accountHistory("1001");

		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(TransactionType.YATIRMA, result.get(0).getTransactionType());
		assertEquals(TransactionType.CEKME, result.get(1).getTransactionType());
		verify(processRepository, times(1)).findByAccountNo("1001");
		// accountHistory() metodu getAccount'u sadece 1 kez çağırıyor (başta), sonra toDto kullanıyor
		verify(accountServiceClient, times(1)).getAccount("1001");
	}
}

