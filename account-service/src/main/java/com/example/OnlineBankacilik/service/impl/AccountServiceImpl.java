package com.example.OnlineBankacilik.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.OnlineBankacilik.dto.AccountRequestDto;
import com.example.OnlineBankacilik.dto.AccountResponseDto;
import com.example.OnlineBankacilik.entity.Account;
import com.example.OnlineBankacilik.entity.Customer;
import com.example.OnlineBankacilik.entity.FixedDepositAccount;
import com.example.OnlineBankacilik.entity.FuturesAccount;
import com.example.OnlineBankacilik.enums.AccountType;
import com.example.OnlineBankacilik.exception.AccountNotFoundException;
import com.example.OnlineBankacilik.exception.CustomerNotFoundException;
import com.example.OnlineBankacilik.repository.AccountRepository;
import com.example.OnlineBankacilik.repository.CustomerRepository;
import com.example.OnlineBankacilik.service.AccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

	private final AccountRepository accountRepository;
	private final CustomerRepository customerRepository;

	private static final AtomicLong COUNTER = new AtomicLong(1000);

	private String newAccountNo() {
		return String.valueOf(COUNTER.incrementAndGet());
	}

	private AccountResponseDto toDto(Account ac) {

		AccountResponseDto dt = new AccountResponseDto();

		dt.setAccountNo(ac.getAccountNo());
		dt.setCustomerId(ac.getCustomer().getId());
		dt.setAmount(ac.getAmount());
		dt.setActive(ac.isActive());
		dt.setOpeningDate(ac.getOpeningDate());
		if (ac instanceof FixedDepositAccount) {
			dt.setAccountType(AccountType.VADESIZ);
		} else if (ac instanceof FuturesAccount fa) {
			dt.setAccountType(AccountType.VADELI);
			dt.setInterestRate(fa.getInterestRate());
			dt.setMaturityDate(fa.getMaturityDate());
			dt.setMaturityMonth(fa.getMaturityMonth());

		}
		return dt;

	}

	@Override
	public AccountResponseDto accountOpen(AccountRequestDto dto) {
		log.info("Hesap açma işlemi başlatıldı: müşteriId={}, hesapTipi={}", dto.getCustomerId(), dto.getAccountType());
		Customer cu = customerRepository.findById(dto.getCustomerId())
				.orElseThrow(() -> new CustomerNotFoundException(dto.getCustomerId()));

		Account acc;
		if (dto.getAccountType() == AccountType.VADESIZ) {
			FixedDepositAccount fda = new FixedDepositAccount();
			acc = fda;
		} else {
			FuturesAccount fa = new FuturesAccount();
			fa.setInterestRate(dto.getInterestRate() == null ? new BigDecimal("0.05") : dto.getInterestRate());
			fa.setMaturityMonth(dto.getMaturityMonth() == null ? 12 : dto.getMaturityMonth());
			fa.setMaturityDate(LocalDate.now().plusMonths(fa.getMaturityMonth()));
			acc = fa;
		}
		acc.setAccountNo(newAccountNo());
		acc.setAmount(dto.getFirstAmount());
		acc.setCustomer(cu);
		Account saved = accountRepository.save(acc);
		log.info("Hesap başarıyla açıldı: hesapNo={}, müşteriId={}, bakiye={}", 
				saved.getAccountNo(), saved.getCustomer().getId(), saved.getAmount());
		return toDto(saved);
	}

	@Override
	public List<AccountResponseDto> allAccounts() {
		List<Account> accounts = accountRepository.findAll();
		if (accounts.isEmpty()) {
			throw new com.example.OnlineBankacilik.exception.AccountNotFoundException("Hesap bulunamdı");
		}

		return accounts.stream().map(this::toDto).toList();
	}

	@Override
	public AccountResponseDto getAccount(String accountNo) {

		return accountRepository.findById(accountNo).map(this::toDto)
				.orElseThrow(() -> new AccountNotFoundException(accountNo));
	}

	@Override
	public List<AccountResponseDto> customerAccounts(Long customerId) {
		var c = customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException(customerId));
		return accountRepository.findByCustomer(c).stream().map(this::toDto).toList();
	}

	@Override
	public void closeAccount(String accountNo) {
		log.info("Hesap kapatma işlemi başlatıldı: hesapNo={}", accountNo);
		Account acc = accountRepository.findById(accountNo).orElseThrow(() -> new AccountNotFoundException(accountNo));
		if (!acc.isActive()) {
			log.warn("Hesap zaten kapalı: hesapNo={}", accountNo);
			throw new RuntimeException("Hesap zaten kapalı");
		}
		acc.setActive(false);
		accountRepository.save(acc);
		log.info("Hesap başarıyla kapatıldı: hesapNo={}", accountNo);
	}

}
