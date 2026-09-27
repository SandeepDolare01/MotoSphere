package com.motosphere.service;

import com.motosphere.dto.response.InternalInvoiceDetailsResponse;
import com.motosphere.dto.response.InvoiceResponse;

public interface InvoiceService {
	InvoiceResponse getInvoiceForJobCard(Long jobCardId);

	// Only ever returns bytes once the invoice is actually PAID - throws
	// BadRequestException otherwise, so the download link can never be used
	// to get an invoice for an unpaid job.
	byte[] getInvoicePdf(Long invoiceId);

	// Used internally (e.g. by InvoiceController) to build the download
	// filename/response headers without a second DB round trip.
	String getInvoiceNumber(Long invoiceId);

	// --- internal, service-to-service only (called by payment-service) ---

	// Everything payment-service needs to create a Razorpay order and verify
	// ownership - it has no Invoice/Garage/Vehicle/User tables of its own.
	InternalInvoiceDetailsResponse getInvoiceDetailsForInternal(Long invoiceId);

	// Called by payment-service once it has independently verified a payment
	// succeeded. Flips the invoice to PAID and sends the invoice email - this
	// service still owns the PDF/email flow since it's the one with the full
	// JobCard/Appointment/Vehicle entity graph needed to render the PDF.
	void markInvoicePaidInternal(Long invoiceId);
}
