package com.motosphere.payment.client;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.motosphere.payment.dto.internal.InvoiceDetails;
import com.motosphere.payment.exception.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Thin wrapper around CoreServiceFeignClient. Kept as its own component (same
// name/public API as before the migration) so PaymentServiceImpl - and every
// other caller - didn't need to change at all; only the transport underneath
// swapped from WebClient to Feign.
@Component
@RequiredArgsConstructor
@Slf4j
public class CoreServiceClient {
	private final CoreServiceFeignClient coreServiceFeignClient;

	public InvoiceDetails getInvoiceDetails(Long invoiceId) {
		try {
			return coreServiceFeignClient.getInvoiceDetails(invoiceId);
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
	//
	// Feign clients are synchronous/blocking (no Mono/reactor pipeline to hang
	// a Retry.backoff(...) operator off of anymore), so the same "3 retries,
	// short backoff" behavior is done here with a plain loop instead.
	private static final int MAX_ATTEMPTS = 4; // 1 initial try + 3 retries, same as before
	private static final long BACKOFF_MILLIS = 500L;

	public void markInvoicePaid(Long invoiceId) {
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
			try {
				coreServiceFeignClient.markInvoicePaid(invoiceId, Map.of());
				return;
			} catch (Exception e) {
				boolean lastAttempt = attempt == MAX_ATTEMPTS;
				log.warn("markInvoicePaid attempt {}/{} failed for invoice {}: {}", attempt, MAX_ATTEMPTS, invoiceId,
						e.getMessage());
				if (lastAttempt) {
					log.error(
							"Could not notify core service that invoice {} is paid, after {} attempts. "
									+ "Payment was recorded locally regardless - this needs manual reconciliation.",
							invoiceId, MAX_ATTEMPTS, e);
					// intentionally NOT rethrown - see javadoc above
					return;
				}
				sleepQuietly(BACKOFF_MILLIS * attempt);
			}
		}
	}

	private void sleepQuietly(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}
}
