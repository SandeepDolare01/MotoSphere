package com.motosphere.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motosphere.service.InvoiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {
	private final InvoiceService invoiceService;

	@GetMapping("/jobcard/{jobCardId}")
	public ResponseEntity<?> getInvoiceForJobCard(@PathVariable Long jobCardId) {
		return ResponseEntity.ok(invoiceService.getInvoiceForJobCard(jobCardId));
	}

	// Only succeeds once the invoice is PAID (enforced in the service layer) -
	// returns the actual PDF bytes for the browser to download.
	@GetMapping("/{invoiceId}/pdf")
	public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long invoiceId) {
		byte[] pdf = invoiceService.getInvoicePdf(invoiceId);
		String invoiceNumber = invoiceService.getInvoiceNumber(invoiceId);

		ContentDisposition disposition = ContentDisposition.attachment()
				.filename(invoiceNumber + ".pdf")
				.build();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentDisposition(disposition);
		headers.setContentType(MediaType.APPLICATION_PDF);

		return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);
	}
}
