package com.example.OnlineBankacilik.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransactionRequestDto {
    private BigDecimal amount;
    private String explanation;
}

