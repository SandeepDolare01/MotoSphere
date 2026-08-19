package com.motosphere.service;

import com.motosphere.entity.Invoice;

public interface InvoicePdfService {
	// Renders a one-page PDF summary of the invoice: header, vehicle/mechanic
	// info, line items, totals. Used both for the customer's download button
	// and as the email attachment sent after a successful payment.
	byte[] generatePdf(Invoice invoice);
}
