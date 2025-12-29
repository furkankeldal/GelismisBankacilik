package com.example.OnlineBankacilik.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.OnlineBankacilik.client.CustomerServiceClient;
import com.example.OnlineBankacilik.dto.AccountRequestDto;
import com.example.OnlineBankacilik.dto.AccountResponseDto;
import com.example.OnlineBankacilik.dto.CustomerResponseDto;
import com.example.OnlineBankacilik.dto.TransactionEvent;
import com.example.OnlineBankacilik.dto.TransactionRequestDto;
import com.example.OnlineBankacilik.entity.Account;
import com.example.OnlineBankacilik.entity.FixedDepositAccount;
import com.example.OnlineBankacilik.entity.FuturesAccount;
import com.example.OnlineBankacilik.enums.AccountType;
import com.example.OnlineBankacilik.exception.AccountNotFoundException;
import com.example.OnlineBankacilik.exception.InsufficientBalanceException;
import com.example.OnlineBankacilik.exception.InvalidAmountException;
import com.example.OnlineBankacilik.repository.AccountRepository;
import com.example.OnlineBankacilik.service.AccountService;
import com.example.OnlineBankacilik.Kafka.TransactionProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

	private final AccountRepository accountRepository;
	private final CustomerServiceClient customerServiceClient;
	private final TransactionProducer transactionProducer;

	private static final AtomicLong COUNTER = new AtomicLong(0);
	private static volatile boolean initialized = false;

	private String newAccountNo() {
		// İlk çağrıda database'den en büyük account_no'yu al ve COUNTER'ı ayarla
		if (!initialized) {
			synchronized (AccountServiceImpl.class) {
				if (!initialized) {
					Long maxAccountNo = accountRepository.findMaxAccountNo();
					if (maxAccountNo != null && maxAccountNo > 0) {
						COUNTER.set(maxAccountNo);
						log.info("Account number counter initialized from database: {}", maxAccountNo);
					} else {
						COUNTER.set(1000); // İlk hesap için 1000'den başla
						log.info("Account number counter initialized to default: 1000");
					}
					initialized = true;
				}
			}
		}
		return String.valueOf(COUNTER.incrementAndGet());
	}

	private AccountResponseDto toDto(Account ac) {

		AccountResponseDto dt = new AccountResponseDto();

		dt.setAccountNo(ac.getAccountNo());
		dt.setCustomerId(ac.getCustomerId());
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
		
		// Customer'ın var olduğunu doğrula (customer-service'ten)
		try {
			CustomerResponseDto customer = customerServiceClient.getCustomerById(dto.getCustomerId());
			if (customer == null) {
				throw new RuntimeException("Müşteri bulunamadı: " + dto.getCustomerId());
			}
			log.info("Müşteri doğrulandı: müşteriId={}, adSoyad={}", dto.getCustomerId(), customer.getNameSurname());
		} catch (Exception e) {
			log.error("Müşteri doğrulama hatası: müşteriId={}", dto.getCustomerId(), e);
			throw new RuntimeException("Müşteri bulunamadı: " + dto.getCustomerId());
		}

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
		acc.setCustomerId(dto.getCustomerId());
		// openingDate'i manuel olarak set et (@PrePersist sadece INSERT'te çalışır)
		if (acc.getOpeningDate() == null) {
			acc.setOpeningDate(LocalDateTime.now());
		}
		Account saved = accountRepository.save(acc);
		log.info("Hesap başarıyla açıldı: hesapNo={}, müşteriId={}, bakiye={}", 
				saved.getAccountNo(), saved.getCustomerId(), saved.getAmount());
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
	@Cacheable(value = "account", key = "#accountNo")
	public AccountResponseDto getAccount(String accountNo) {
		log.debug("Account bilgisi DB'den alınıyor: accountNo={}", accountNo);
		return accountRepository.findById(accountNo).map(this::toDto)
				.orElseThrow(() -> new AccountNotFoundException(accountNo));
	}

	@Override
	@Cacheable(value = "customerAccounts", key = "#customerId")
	public List<AccountResponseDto> customerAccounts(Long customerId) {
		// Customer'ın var olduğunu doğrula
		try {
			CustomerResponseDto customer = customerServiceClient.getCustomerById(customerId);
			if (customer == null) {
				throw new RuntimeException("Müşteri bulunamadı: " + customerId);
			}
		} catch (Exception e) {
			log.error("Müşteri doğrulama hatası: müşteriId={}", customerId, e);
			throw new RuntimeException("Müşteri bulunamadı: " + customerId);
		}
		log.debug("Müşteri hesapları DB'den alınıyor: customerId={}", customerId);
		return accountRepository.findByCustomerId(customerId).stream().map(this::toDto).toList();
	}

	@Override
	@CacheEvict(value = {"account", "customerAccounts"}, allEntries = true)
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

	@Override
	@CacheEvict(value = {"account", "customerAccounts"}, allEntries = true)
	public AccountResponseDto deposit(String accountNo, TransactionRequestDto request) {
		log.info("Para yatırma işlemi başlatıldı: hesapNo={}, tutar={}", accountNo, request.getAmount());
		Account account = accountRepository.findById(accountNo)
				.orElseThrow(() -> new AccountNotFoundException(accountNo));
		
		if (!account.isActive()) {
			log.warn("Kapalı hesaba para yatırma denemesi: hesapNo={}", accountNo);
			throw new RuntimeException("Kapalı hesaba işlem yapılamaz");
		}
		
		if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			log.warn("Geçersiz tutar: hesapNo={}, tutar={}", accountNo, request.getAmount());
			throw new InvalidAmountException();
		}
		
		BigDecimal previousBalance = account.getAmount();
		account.deposit(request.getAmount());
		Account saved = accountRepository.save(account);
		
		// Kafka notification
		transactionProducer.publish(new TransactionEvent(
				java.util.UUID.randomUUID().toString(),
				saved.getAccountNo(),
				saved.getCustomerId(),
				com.example.OnlineBankacilik.enums.TransactionType.YATIRMA,
				request.getAmount(),
				previousBalance,
				saved.getAmount(),
				true,
				LocalDateTime.now()
		));

		log.info("Para yatırma işlemi başarılı: hesapNo={}, yeniBakiye={}", accountNo, saved.getAmount());
		return toDto(saved);
	}

	@Override
	@CacheEvict(value = {"account", "customerAccounts"}, allEntries = true)
	public AccountResponseDto withdraw(String accountNo, TransactionRequestDto request) {
		log.info("Para çekme işlemi başlatıldı: hesapNo={}, tutar={}", accountNo, request.getAmount());
		Account account = accountRepository.findById(accountNo)
				.orElseThrow(() -> new AccountNotFoundException(accountNo));
		
		if (!account.isActive()) {
			log.warn("Kapalı hesaptan para çekme denemesi: hesapNo={}", accountNo);
			throw new RuntimeException("Kapalı hesaptan işlem yapılamaz");
		}
		
		if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			log.warn("Geçersiz tutar: hesapNo={}, tutar={}", accountNo, request.getAmount());
			throw new InvalidAmountException();
		}
		
		if (account.getAmount().compareTo(request.getAmount()) < 0) {
			log.warn("Yetersiz bakiye: hesapNo={}, mevcut={}, istenen={}", 
					accountNo, account.getAmount(), request.getAmount());
			throw new InsufficientBalanceException(account.getAmount(), request.getAmount());
		}
		
		BigDecimal previousBalance = account.getAmount();
		account.withdraw(request.getAmount());
		Account saved = accountRepository.save(account);

		// Kafka notification
		transactionProducer.publish(new TransactionEvent(
				java.util.UUID.randomUUID().toString(),
				saved.getAccountNo(),
				saved.getCustomerId(),
				com.example.OnlineBankacilik.enums.TransactionType.CEKME,
				request.getAmount(),
				previousBalance,
				saved.getAmount(),
				true,
				LocalDateTime.now()
		));

		log.info("Para çekme işlemi başarılı: hesapNo={}, yeniBakiye={}", accountNo, saved.getAmount());
		return toDto(saved);
	}

	@Override
	@CacheEvict(value = {"account", "customerAccounts"}, allEntries = true)
	public AccountResponseDto processInterest(String accountNo) {
		log.info("Faiz işlemi başlatıldı: hesapNo={}", accountNo);
		Account account = accountRepository.findById(accountNo)
				.orElseThrow(() -> new AccountNotFoundException(accountNo));
		
		if (!account.isActive()) {
			log.warn("Kapalı hesapta faiz işlemi denemesi: hesapNo={}", accountNo);
			throw new RuntimeException("Kapalı hesapta işlem yapılamaz");
		}
		
		if (!(account instanceof FuturesAccount)) {
			log.warn("Vadeli olmayan hesapta faiz işlemi denemesi: hesapNo={}", accountNo);
			throw new RuntimeException("Sadece vadeli hesapta faiz işlemi yapılabilir");
		}
		
		FuturesAccount futuresAccount = (FuturesAccount) account;
		futuresAccount.interestProcessing();
		Account saved = accountRepository.save(futuresAccount);
		log.info("Faiz işlemi başarılı: hesapNo={}, yeniBakiye={}", accountNo, saved.getAmount());
		return toDto(saved);
	}

}
