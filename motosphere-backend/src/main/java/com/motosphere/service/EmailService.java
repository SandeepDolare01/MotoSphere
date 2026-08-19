package com.motosphere.service;

import com.motosphere.entity.Invoice;

public interface EmailService {
	void sendInvoiceEmail(Invoice invoice, String toEmail);
}
