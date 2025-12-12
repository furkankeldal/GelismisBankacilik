package com.example.OnlineBankacilik.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.OnlineBankacilik.Kafka.TransactionProducer;
import com.example.OnlineBankacilik.dto.ProcessRequestDto;
import com.example.OnlineBankacilik.dto.ProcessResponseDto;
import com.example.OnlineBankacilik.entity.Account;
import com.example.OnlineBankacilik.entity.FuturesAccount;
import com.example.OnlineBankacilik.entity.Process;
import com.example.OnlineBankacilik.dto.TransactionEvent;
import com.example.OnlineBankacilik.enums.TransactionType;
import com.example.OnlineBankacilik.exception.AccountNotFoundException;
import com.example.OnlineBankacilik.exception.InsufficientBalanceException;
import com.example.OnlineBankacilik.exception.InvalidAccountTypeException;
import com.example.OnlineBankacilik.exception.InvalidAmountException;
import com.example.OnlineBankacilik.repository.AccountRepository;
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
	private final AccountRepository accountRepository;
	private final TransactionProducer transactionProducer;

	private Account getAccount(String accountNo) {
		return accountRepository.findById(accountNo).orElseThrow(() -> new AccountNotFoundException(accountNo));
	}

	private String nextTxnCode() {
		Long c = processRepository.count() + 1;
		return String.format("TXN-%03d", c);

	}

	private ProcessResponseDto toDto(Process pr) {
		ProcessResponseDto prd = new ProcessResponseDto();
		prd.setCustomerId(pr.getAccount().getCustomer().getId());
		prd.setNumberOfAccount(pr.getAccount().getAccountNo());
		prd.setTransactionType(pr.getTransactionType());
		prd.setRegistrationDate(pr.getTransactionDate());
		prd.setAmount(pr.getAmount());
		prd.setPreviousBalance(pr.getPreviousBalance());
		prd.setNewBalance(pr.getNewBalance());
		prd.setExplanation(pr.getExplanation());
		prd.setSuccesfull(pr.isSuccessful());

		return prd;

	}

	@Override
	public ProcessResponseDto deposit(ProcessRequestDto dto) {
		if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0)
			throw new InvalidAmountException();
		Account ac = getAccount(dto.getAccountNo());
		if (!ac.isActive()) {
			log.warn("Kapalı hesaba para yatırma denemesi: hesapNo={}", dto.getAccountNo());
			throw new RuntimeException("Kapalı hesaba işlem yapılamaz");
		}
		BigDecimal former = ac.getAmount();
		ac.deposit(dto.getAmount());
		accountRepository.save(ac);

		Process process = new Process();
		process.setTransactionCode(nextTxnCode());
		process.setAccount(ac);
		process.setAmount(dto.getAmount());
		process.setTransactionType(TransactionType.YATIRMA);
		process.setNewBalance(ac.getAmount());
		process.setPreviousBalance(former);
		process.setExplanation(dto.getExplanation());
		process.setSuccessful(true);
		Process saved = processRepository.save(process);

		TransactionEvent event = new TransactionEvent(saved.getTransactionCode(), // transactionId
				ac.getAccountNo(), // accountNo
				ac.getCustomer().getId(), // customerId
				saved.getTransactionType(), // transactionType
				saved.getAmount(), // amount
				saved.getPreviousBalance(), // previousBalance
				saved.getNewBalance(), // newBalance
				saved.isSuccessful(), // successful
				saved.getTransactionDate() // transactionDate
		);

		transactionProducer.publish(event);
		log.info("Kafka'ya gönderilen deposit event: {}", event);

		return toDto(saved);
	}

	@Override
	public ProcessResponseDto withdraw(ProcessRequestDto dto) {
		if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0)
			throw new InvalidAmountException();
		Account account = getAccount(dto.getAccountNo());
		if (!account.isActive()) {
			log.warn("Kapalı hesaptan para çekme denemesi: hesapNo={}", dto.getAccountNo());
			throw new RuntimeException("Kapalı hesaptan işlem yapılamaz");
		}
		BigDecimal former = account.getAmount();
		if (former.compareTo(dto.getAmount()) < 0)
			throw new InsufficientBalanceException(former, dto.getAmount());
		account.withdraw(dto.getAmount());
		accountRepository.save(account);

		Process process = new Process();
		process.setTransactionCode(nextTxnCode());
		process.setAccount(account);
		process.setAmount(dto.getAmount());
		process.setTransactionType(TransactionType.CEKME);
		process.setNewBalance(account.getAmount());
		process.setPreviousBalance(former);
		process.setExplanation(dto.getExplanation());
		process.setSuccessful(true);
		Process saved = processRepository.save(process);

		TransactionEvent event = new TransactionEvent(saved.getTransactionCode(), // transactionId (String)
				account.getAccountNo(), // accountNo (String)
				account.getCustomer().getId(), // customerId (Long)
				saved.getTransactionType(), // CEKME
				saved.getAmount(), // amount
				saved.getPreviousBalance(), // previousBalance
				saved.getNewBalance(), // newBalance
				saved.isSuccessful(), // successful
				saved.getTransactionDate() // transactionDate
		);

		transactionProducer.publish(event);
		log.info("Kafka'ya gönderilen withdraw event: {}", event);

		return toDto(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public ProcessResponseDto amount(String accountNo) {
		Account acc = getAccount(accountNo);
		ProcessResponseDto prd = new ProcessResponseDto();
		prd.setNumberOfAccount(accountNo);
		prd.setNewBalance(acc.getAmount());
		prd.setTransactionType(TransactionType.YATIRMA);
		return prd;
	}

	@Override
	public ProcessResponseDto earnInterest(String accountNo) {
		Account a = getAccount(accountNo);
		if (!a.isActive()) {
			log.warn("Kapalı hesapta faiz işlemi denemesi: hesapNo={}", accountNo);
			throw new RuntimeException("Kapalı hesapta işlem yapılamaz");
		}
		if (!(a instanceof FuturesAccount fa)) {
			throw new InvalidAccountTypeException("Sadece vadeli hesapta faiz işlemi yapılabilir");
		}
		BigDecimal former = a.getAmount();
		fa.interestProcessing();
		accountRepository.save(fa);
		Process process = new Process();
		process.setAccount(a);
		process.setTransactionCode(nextTxnCode());
		process.setAmount(a.getAmount().subtract(former));
		process.setTransactionType(TransactionType.FAIZ_ISLEME);
		process.setNewBalance(a.getAmount());
		process.setPreviousBalance(former);

		var dto = toDto(processRepository.save(process));
		dto.setInterestRate(fa.getInterestRate());
		return dto;

	}

	@Override
	public List<ProcessResponseDto> accountHistory(String accountNo) {
		Account acc = getAccount(accountNo);
		return processRepository.findByAccount(acc).stream().map(this::toDto).toList();
	}

}
