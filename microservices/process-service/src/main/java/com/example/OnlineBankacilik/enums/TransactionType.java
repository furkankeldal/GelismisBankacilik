package com.example.OnlineBankacilik.enums;

public enum TransactionType {
	YATIRMA("Para Yatırma"), CEKME("Para Çekme"), FAIZ_ISLEME("Faiz İşleme"), TRANSFER("Transfer");

	private final String explanation;

	TransactionType(String explanation) {
		this.explanation = explanation;
	}

	public String getExplanation() {
		return explanation;
	}
}

