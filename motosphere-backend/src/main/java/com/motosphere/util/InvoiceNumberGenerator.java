package com.motosphere.util;

import org.springframework.stereotype.Component;

@Component
public class InvoiceNumberGenerator {

	public String generate(Long jobCardId) {
		return "INV-" + jobCardId + "-" + System.currentTimeMillis();
	}

}
