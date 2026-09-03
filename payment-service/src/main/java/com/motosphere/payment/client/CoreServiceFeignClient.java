package com.motosphere.payment.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.motosphere.payment.config.CoreServiceFeignConfig;
import com.motosphere.payment.dto.internal.InvoiceDetails;

// Declarative replacement for the old CoreServiceClient + WebClient combo.
// The "name" just needs to be unique across registered Feign clients; since
// we're not using service discovery (Eureka/Consul), "url" pins this
// directly at motosphere-backend's base URL from application.properties.
// The X-Internal-Api-Key header is NOT declared here - it's added
// automatically to every call by CoreServiceFeignConfig's RequestInterceptor,
// so it doesn't need to be repeated on each method signature below.
@FeignClient(name = "core-service", url = "${core.service.base-url}", configuration = CoreServiceFeignConfig.class)
public interface CoreServiceFeignClient {

	@GetMapping("/internal/invoices/{invoiceId}")
	InvoiceDetails getInvoiceDetails(@PathVariable("invoiceId") Long invoiceId);

	// IMPORTANT: this MUST have a @RequestBody, even an empty one. OkHttp
	// (which we're using as Feign's HTTP client so PATCH works at all - see
	// pom.xml/application.properties) refuses to build a PATCH/POST/PUT
	// request with a null body and throws
	// "IllegalArgumentException: method PATCH must have a request body"
	// before the request is even sent. motosphere-backend's controller
	// method has no @RequestBody parameter, so it simply ignores whatever
	// we send here - this body exists purely to satisfy OkHttp, not because
	// the backend needs any data from it.
	@PatchMapping("/internal/invoices/{invoiceId}/mark-paid")
	void markInvoicePaid(@PathVariable("invoiceId") Long invoiceId, @RequestBody Map<String, Object> body);
}

