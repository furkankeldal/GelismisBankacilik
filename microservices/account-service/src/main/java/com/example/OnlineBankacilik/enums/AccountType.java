package com.example.OnlineBankacilik.enums;

public enum AccountType {

	VADESIZ("Vadesiz Hesap"), VADELI("Vadeli Hesap");

	private final String explanation;

	AccountType(String explanation) {
		this.explanation = explanation;
	}

	public String getExplanation() {
		return explanation;
	}
}
