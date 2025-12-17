package com.example.OnlineBankacilik.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import com.example.OnlineBankacilik.client.CustomerServiceClient;
import com.example.OnlineBankacilik.dto.AccountRequestDto;
import com.example.OnlineBankacilik.dto.AccountResponseDto;
import com.example.OnlineBankacilik.dto.CustomerResponseDto;
import com.example.OnlineBankacilik.dto.TransactionRequestDto;
import com.example.OnlineBankacilik.entity.FixedDepositAccount;
import com.example.OnlineBankacilik.entity.FuturesAccount;
import com.example.OnlineBankacilik.enums.AccountType;
import com.example.OnlineBankacilik.exception.AccountNotFoundException;
import com.example.OnlineBankacilik.repository.AccountRepository;
import com.example.OnlineBankacilik.service.impl.AccountServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Account Service Unit Tests")
class AccountServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private CustomerServiceClient customerServiceClient;

	@InjectMocks
	private AccountServiceImpl accountService;

	private CustomerResponseDto testCustomer;
	private AccountRequestDto testAccountRequest;
	private FixedDepositAccount testAccount;
	private LocalDateTime testDate;

	@BeforeEach
	void setUp() {
		testDate = LocalDateTime.now();

		testCustomer = new CustomerResponseDto();
		testCustomer.setCustomerId(1L);
		testCustomer.setNameSurname("Ali Veli");
		testCustomer.setTcKimlikNo("12345678901");
		testCustomer.setEmail("ali.veli@example.com");

		testAccountRequest = new AccountRequestDto();
		testAccountRequest.setCustomerId(1L);
		testAccountRequest.setAccountType(AccountType.VADESIZ);
		testAccountRequest.setFirstAmount(new BigDecimal("1000.00"));

		testAccount = new FixedDepositAccount();
		testAccount.setAccountNo("1001");
		testAccount.setCustomerId(1L);
		testAccount.setAmount(new BigDecimal("1000.00"));
		testAccount.setActive(true);
		testAccount.setOpeningDate(testDate);
	}

	@Test
	@DisplayName("Should open VADESIZ account successfully")
	void testAccountOpen_Vadesiz_Success() {
		// Given
		when(customerServiceClient.getCustomerById(1L)).thenReturn(testCustomer);
		when(accountRepository.save(any(FixedDepositAccount.class))).thenAnswer(invocation -> {
			FixedDepositAccount account = invocation.getArgument(0);
			account.setAccountNo("1001");
			return account;
		});

		// When
		AccountResponseDto result = accountService.accountOpen(testAccountRequest);

		// Then
		assertNotNull(result);
		assertNotNull(result.getAccountNo());
		assertEquals(1L, result.getCustomerId());
		assertEquals(AccountType.VADESIZ, result.getAccountType());
		assertEquals(new BigDecimal("1000.00"), result.getAmount());
		assertTrue(result.isActive());
		verify(customerServiceClient, times(1)).getCustomerById(1L);
		verify(accountRepository, times(1)).save(any(FixedDepositAccount.class));
	}

	@Test
	@DisplayName("Should open VADELI account successfully")
	void testAccountOpen_Vadeli_Success() {
		// Given
		AccountRequestDto vadeliRequest = new AccountRequestDto();
		vadeliRequest.setCustomerId(1L);
		vadeliRequest.setAccountType(AccountType.VADELI);
		vadeliRequest.setFirstAmount(new BigDecimal("5000.00"));
		vadeliRequest.setInterestRate(new BigDecimal("0.06"));
		vadeliRequest.setMaturityMonth(12);

		when(customerServiceClient.getCustomerById(1L)).thenReturn(testCustomer);
		when(accountRepository.save(any(FuturesAccount.class))).thenAnswer(invocation -> {
			FuturesAccount account = invocation.getArgument(0);
			account.setAccountNo("1002");
			return account;
		});

		// When
		AccountResponseDto result = accountService.accountOpen(vadeliRequest);

		// Then
		assertNotNull(result);
		assertEquals(AccountType.VADELI, result.getAccountType());
		assertEquals(new BigDecimal("0.06"), result.getInterestRate());
		assertEquals(12, result.getMaturityMonth());
		assertNotNull(result.getMaturityDate());
		verify(customerServiceClient, times(1)).getCustomerById(1L);
		verify(accountRepository, times(1)).save(any(FuturesAccount.class));
	}

	@Test
	@DisplayName("Should throw exception when customer not found")
	void testAccountOpen_CustomerNotFound_ThrowsException() {
		// Given
		when(customerServiceClient.getCustomerById(999L)).thenReturn(null);

		// When & Then
		assertThrows(RuntimeException.class, () -> accountService.accountOpen(testAccountRequest));
		verify(customerServiceClient, times(1)).getCustomerById(1L);
		verify(accountRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should get account by account number")
	void testGetAccount_Success() {
		// Given
		when(accountRepository.findById("1001")).thenReturn(Optional.of(testAccount));

		// When
		AccountResponseDto result = accountService.getAccount("1001");

		// Then
		assertNotNull(result);
		assertEquals("1001", result.getAccountNo());
		assertEquals(1L, result.getCustomerId());
		verify(accountRepository, times(1)).findById("1001");
	}

	@Test
	@DisplayName("Should throw exception when account not found")
	void testGetAccount_NotFound_ThrowsException() {
		// Given
		when(accountRepository.findById("9999")).thenReturn(Optional.empty());

		// When & Then
		assertThrows(AccountNotFoundException.class, () -> accountService.getAccount("9999"));
		verify(accountRepository, times(1)).findById("9999");
	}

	@Test
	@DisplayName("Should deposit money successfully")
	void testDeposit_Success() {
		// Given
		TransactionRequestDto depositRequest = new TransactionRequestDto();
		depositRequest.setAmount(new BigDecimal("500.00"));
		depositRequest.setExplanation("Test deposit");

		FixedDepositAccount accountWithDeposit = new FixedDepositAccount();
		accountWithDeposit.setAccountNo("1001");
		accountWithDeposit.setCustomerId(1L);
		accountWithDeposit.setAmount(new BigDecimal("1500.00"));
		accountWithDeposit.setActive(true);
		accountWithDeposit.setOpeningDate(testDate);

		when(accountRepository.findById("1001")).thenReturn(Optional.of(testAccount));
		when(accountRepository.save(any(FixedDepositAccount.class))).thenReturn(accountWithDeposit);

		// When
		AccountResponseDto result = accountService.deposit("1001", depositRequest);

		// Then
		assertNotNull(result);
		assertEquals(new BigDecimal("1500.00"), result.getAmount());
		verify(accountRepository, times(1)).findById("1001");
		verify(accountRepository, times(1)).save(any(FixedDepositAccount.class));
	}

	@Test
	@DisplayName("Should withdraw money successfully")
	void testWithdraw_Success() {
		// Given
		TransactionRequestDto withdrawRequest = new TransactionRequestDto();
		withdrawRequest.setAmount(new BigDecimal("200.00"));
		withdrawRequest.setExplanation("Test withdraw");

		FixedDepositAccount accountAfterWithdraw = new FixedDepositAccount();
		accountAfterWithdraw.setAccountNo("1001");
		accountAfterWithdraw.setCustomerId(1L);
		accountAfterWithdraw.setAmount(new BigDecimal("800.00"));
		accountAfterWithdraw.setActive(true);
		accountAfterWithdraw.setOpeningDate(testDate);

		when(accountRepository.findById("1001")).thenReturn(Optional.of(testAccount));
		when(accountRepository.save(any(FixedDepositAccount.class))).thenReturn(accountAfterWithdraw);

		// When
		AccountResponseDto result = accountService.withdraw("1001", withdrawRequest);

		// Then
		assertNotNull(result);
		assertEquals(new BigDecimal("800.00"), result.getAmount());
		verify(accountRepository, times(1)).findById("1001");
		verify(accountRepository, times(1)).save(any(FixedDepositAccount.class));
	}

	@Test
	@DisplayName("Should throw exception when withdrawing more than balance")
	void testWithdraw_InsufficientBalance_ThrowsException() {
		// Given
		TransactionRequestDto withdrawRequest = new TransactionRequestDto();
		withdrawRequest.setAmount(new BigDecimal("2000.00"));
		withdrawRequest.setExplanation("Test withdraw");

		when(accountRepository.findById("1001")).thenReturn(Optional.of(testAccount));

		// When & Then
		assertThrows(RuntimeException.class, () -> accountService.withdraw("1001", withdrawRequest));
		verify(accountRepository, times(1)).findById("1001");
		verify(accountRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should get all accounts")
	void testAllAccounts_Success() {
		// Given
		FixedDepositAccount account2 = new FixedDepositAccount();
		account2.setAccountNo("1002");
		account2.setCustomerId(2L);
		account2.setAmount(new BigDecimal("2000.00"));
		account2.setActive(true);
		account2.setOpeningDate(testDate);

		List<com.example.OnlineBankacilik.entity.Account> accounts = Arrays.asList(testAccount, account2);
		when(accountRepository.findAll()).thenReturn(accounts);

		// When
		List<AccountResponseDto> result = accountService.allAccounts();

		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("1001", result.get(0).getAccountNo());
		assertEquals("1002", result.get(1).getAccountNo());
		verify(accountRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("Should get customer accounts")
	void testCustomerAccounts_Success() {
		// Given
		when(customerServiceClient.getCustomerById(1L)).thenReturn(testCustomer);
		when(accountRepository.findByCustomerId(1L)).thenReturn(Arrays.asList(testAccount));

		// When
		List<AccountResponseDto> result = accountService.customerAccounts(1L);

		// Then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("1001", result.get(0).getAccountNo());
		verify(customerServiceClient, times(1)).getCustomerById(1L);
		verify(accountRepository, times(1)).findByCustomerId(1L);
	}

	@Test
	@DisplayName("Should close account successfully")
	void testCloseAccount_Success() {
		// Given
		testAccount.setActive(true); // Account is active initially
		FixedDepositAccount closedAccount = new FixedDepositAccount();
		closedAccount.setAccountNo("1001");
		closedAccount.setCustomerId(1L);
		closedAccount.setAmount(new BigDecimal("1000.00"));
		closedAccount.setActive(false); // Account is closed after operation
		closedAccount.setOpeningDate(testDate);
		
		when(accountRepository.findById("1001")).thenReturn(Optional.of(testAccount));
		when(accountRepository.save(any(FixedDepositAccount.class))).thenReturn(closedAccount);

		// When
		accountService.closeAccount("1001");

		// Then
		verify(accountRepository, times(1)).findById("1001");
		verify(accountRepository, times(1)).save(any(FixedDepositAccount.class));
	}
}

