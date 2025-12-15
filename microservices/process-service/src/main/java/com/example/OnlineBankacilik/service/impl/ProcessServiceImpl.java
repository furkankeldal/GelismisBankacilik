package com.example.OnlineBankacilik.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.OnlineBankacilik.Kafka.TransactionProducer;
import com.example.OnlineBankacilik.client.AccountServiceClient;
import com.example.OnlineBankacilik.dto.AccountResponseDto;
import com.example.OnlineBankacilik.dto.ProcessRequestDto;
import com.example.OnlineBankacilik.dto.ProcessResponseDto;
import com.example.OnlineBankacilik.dto.TransactionEvent;
import com.example.OnlineBankacilik.dto.TransactionRequestDto;
import com.example.OnlineBankacilik.enums.AccountType;
import com.example.OnlineBankacilik.enums.TransactionType;
import com.example.OnlineBankacilik.entity.Process;
import com.example.OnlineBankacilik.exception.AccountNotFoundException;
import com.example.OnlineBankacilik.exception.InsufficientBalanceException;
import com.example.OnlineBankacilik.exception.InvalidAccountTypeException;
import com.example.OnlineBankacilik.exception.InvalidAmountException;
import com.example.OnlineBankacilik.repository.ProcessRepository;
import com.example.OnlineBankacilik.service.ProcessService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProcessServiceImpl implements ProcessService {

	private final ProcessRepository processRepository;
	private final AccountServiceClient accountServiceClient;
	private final TransactionProducer transactionProducer;

	private AccountResponseDto getAccount(String accountNo) {
		try {
			return accountServiceClient.getAccount(accountNo);
		} catch (Exception e) {
			log.error("Hesap bulunamadı: {}", accountNo, e);
			throw new AccountNotFoundException(accountNo);
		}
	}

	private String nextTxnCode() {
		Long c = processRepository.count() + 1;
		return String.format("TXN-%03d", c);
	}

	private ProcessResponseDto toDto(Process pr, AccountResponseDto account) {
		ProcessResponseDto prd = new ProcessResponseDto();
		prd.setCustomerId(account.getCustomerId());
		prd.setNumberOfAccount(pr.getAccountNo());
		prd.setTransactionType(pr.getTransactionType());
		prd.setRegistrationDate(pr.getTransactionDate());
		prd.setAmount(pr.getAmount());
		prd.setPreviousBalance(pr.getPreviousBalance());
		prd.setNewBalance(pr.getNewBalance());
		prd.setExplanation(pr.getExplanation());
		prd.setSuccesfull(pr.isSuccessful());
		if (account.getInterestRate() != null) {
			prd.setInterestRate(account.getInterestRate());
		}
		return prd;
	}

	@Override
	public ProcessResponseDto deposit(ProcessRequestDto dto) {
		if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0)
			throw new InvalidAmountException();
		
		AccountResponseDto account = getAccount(dto.getAccountNo());
		if (!account.isActive()) {
			log.warn("Kapalı hesaba para yatırma denemesi: hesapNo={}", dto.getAccountNo());
			throw new RuntimeException("Kapalı hesaba işlem yapılamaz");
		}
		
		BigDecimal former = account.getAmount();
		
		// Call account-service to deposit
		TransactionRequestDto transactionRequest = new TransactionRequestDto();
		transactionRequest.setAmount(dto.getAmount());
		transactionRequest.setExplanation(dto.getExplanation());
		AccountResponseDto updatedAccount = accountServiceClient.deposit(dto.getAccountNo(), transactionRequest);
		
		Process process = new Process();
		process.setTransactionCode(nextTxnCode());
		process.setAccountNo(dto.getAccountNo());
		process.setCustomerId(account.getCustomerId());
		process.setAmount(dto.getAmount());
		process.setTransactionType(TransactionType.YATIRMA);
		process.setNewBalance(updatedAccount.getAmount());
		process.setPreviousBalance(former);
		process.setExplanation(dto.getExplanation());
		process.setSuccessful(true);
		Process saved = processRepository.save(process);

		TransactionEvent event = new TransactionEvent(saved.getTransactionCode(),
				saved.getAccountNo(),
				saved.getCustomerId(),
				saved.getTransactionType(),
				saved.getAmount(),
				saved.getPreviousBalance(),
				saved.getNewBalance(),
				saved.isSuccessful(),
				saved.getTransactionDate()
		);

		transactionProducer.publish(event);
		log.info("Kafka'ya gönderilen deposit event: {}", event);

		return toDto(saved, updatedAccount);
	}

	@Override
	public ProcessResponseDto withdraw(ProcessRequestDto dto) {
		if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0)
			throw new InvalidAmountException();
		
		AccountResponseDto account = getAccount(dto.getAccountNo());
		if (!account.isActive()) {
			log.warn("Kapalı hesaptan para çekme denemesi: hesapNo={}", dto.getAccountNo());
			throw new RuntimeException("Kapalı hesaptan işlem yapılamaz");
		}
		
		BigDecimal former = account.getAmount();
		if (former.compareTo(dto.getAmount()) < 0)
			throw new InsufficientBalanceException(former, dto.getAmount());
		
		// Call account-service to withdraw
		TransactionRequestDto transactionRequest = new TransactionRequestDto();
		transactionRequest.setAmount(dto.getAmount());
		transactionRequest.setExplanation(dto.getExplanation());
		AccountResponseDto updatedAccount = accountServiceClient.withdraw(dto.getAccountNo(), transactionRequest);

		Process process = new Process();
		process.setTransactionCode(nextTxnCode());
		process.setAccountNo(dto.getAccountNo());
		process.setCustomerId(account.getCustomerId());
		process.setAmount(dto.getAmount());
		process.setTransactionType(TransactionType.CEKME);
		process.setNewBalance(updatedAccount.getAmount());
		process.setPreviousBalance(former);
		process.setExplanation(dto.getExplanation());
		process.setSuccessful(true);
		Process saved = processRepository.save(process);

		TransactionEvent event = new TransactionEvent(saved.getTransactionCode(),
				saved.getAccountNo(),
				saved.getCustomerId(),
				saved.getTransactionType(),
				saved.getAmount(),
				saved.getPreviousBalance(),
				saved.getNewBalance(),
				saved.isSuccessful(),
				saved.getTransactionDate()
		);

		transactionProducer.publish(event);
		log.info("Kafka'ya gönderilen withdraw event: {}", event);

		return toDto(saved, updatedAccount);
	}

	@Override
	@Transactional(readOnly = true)
	public ProcessResponseDto amount(String accountNo) {
		AccountResponseDto account = getAccount(accountNo);
		ProcessResponseDto prd = new ProcessResponseDto();
		prd.setNumberOfAccount(accountNo);
		prd.setNewBalance(account.getAmount());
		prd.setTransactionType(TransactionType.YATIRMA);
		return prd;
	}

	@Override
	public ProcessResponseDto earnInterest(String accountNo) {
		AccountResponseDto account = getAccount(accountNo);
		if (!account.isActive()) {
			log.warn("Kapalı hesapta faiz işlemi denemesi: hesapNo={}", accountNo);
			throw new RuntimeException("Kapalı hesapta işlem yapılamaz");
		}
		if (account.getAccountType() != AccountType.VADELI) {
			throw new InvalidAccountTypeException("Sadece vadeli hesapta faiz işlemi yapılabilir");
		}
		
		BigDecimal former = account.getAmount();
		
		// Call account-service to process interest
		AccountResponseDto updatedAccount = accountServiceClient.processInterest(accountNo);
		
		Process process = new Process();
		process.setAccountNo(accountNo);
		process.setCustomerId(account.getCustomerId());
		process.setTransactionCode(nextTxnCode());
		process.setAmount(updatedAccount.getAmount().subtract(former));
		process.setTransactionType(TransactionType.FAIZ_ISLEME);
		process.setNewBalance(updatedAccount.getAmount());
		process.setPreviousBalance(former);

		Process saved = processRepository.save(process);
		var dto = toDto(saved, updatedAccount);
		dto.setInterestRate(updatedAccount.getInterestRate());
		return dto;
	}

	@Override
	public List<ProcessResponseDto> accountHistory(String accountNo) {
		AccountResponseDto account = getAccount(accountNo);
		return processRepository.findByAccountNo(accountNo).stream()
				.map(pr -> toDto(pr, account))
				.toList();
	}

}

