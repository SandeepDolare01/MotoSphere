package com.motosphere.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.motosphere.payment.dto.internal.InvoiceDetails;
import com.motosphere.payment.exception.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoreServiceClient {
	private final WebClient coreServiceWebClient;

	@Value("${internal.api.key}")
	private String internalApiKey;

	public InvoiceDetails getInvoiceDetails(Long invoiceId) {
		try {
			return coreServiceWebClient.get().uri("/internal/invoices/{invoiceId}", invoiceId)
					.header("X-Internal-Api-Key", internalApiKey).retrieve().bodyToMono(InvoiceDetails.class)
					.block();
		} catch (Exception e) {
			log.error("Failed to fetch invoice {} from core service: {}", invoiceId, e.getMessage());
			throw new BadRequestException("Could not verify this invoice right now - please try again");
		}
	}

	// The payment has ALREADY succeeded on Razorpay's side by the time this is
	// called - a transient failure here must never be shown to the customer as
	// "payment failed" (it didn't; the money moved). Retry a few times before
	// giving up, and if it still fails, the payment record we already saved
	// locally is enough for support to manually reconcile later - see the
	// workflow notes on eventual consistency.
	public void markInvoicePaid(Long invoiceId) {
		try {
			coreServiceWebClient.patch().uri("/internal/invoices/{invoiceId}/mark-paid", invoiceId)
					.header("X-Internal-Api-Key", internalApiKey).retrieve().toBodilessEntity()
					.retryWhen(Retry.backoff(3, Duration.ofMillis(500))).block();
		} catch (Exception e) {
			log.error(
					"Could not notify core service that invoice {} is paid, after retries: {}. "
							+ "Payment was recorded locally regardless - this needs manual reconciliation.",
					invoiceId, e.getMessage());
			// intentionally NOT rethrown - see javadoc above
		}
	}
}
