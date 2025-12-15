package com.example.OnlineBankacilik.service;

import java.util.List;

import com.example.OnlineBankacilik.dto.ProcessRequestDto;
import com.example.OnlineBankacilik.dto.ProcessResponseDto;

public interface ProcessService {

	ProcessResponseDto deposit(ProcessRequestDto dto);

	ProcessResponseDto withdraw(ProcessRequestDto dto);

	ProcessResponseDto amount(String accountNo);

	ProcessResponseDto earnInterest(String accountNo);

	List<ProcessResponseDto> accountHistory(String accountNo);
}

