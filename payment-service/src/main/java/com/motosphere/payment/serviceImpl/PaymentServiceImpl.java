package com.motosphere.payment.serviceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motosphere.payment.client.CoreServiceClient;
import com.motosphere.payment.dto.internal.InvoiceDetails;
import com.motosphere.payment.dto.request.PaymentRequest;
import com.motosphere.payment.dto.request.RazorpayVerifyRequest;
import com.motosphere.payment.dto.response.AdminDashboardResponse;
import com.motosphere.payment.dto.response.GarageEarningsResponse;
import com.motosphere.payment.dto.response.ManagerDashboardResponse;
import com.motosphere.payment.dto.response.PaymentResponse;
import com.motosphere.payment.dto.response.RazorpayOrderResponse;
import com.motosphere.payment.dto.response.TransactionResponse;
import com.motosphere.payment.entity.Payment;
import com.motosphere.payment.enums.PaymentMethod;
import com.motosphere.payment.exception.BadRequestException;
import com.motosphere.payment.exception.UnauthorizedActionException;
import com.motosphere.payment.repository.PaymentRepository;
import com.motosphere.payment.service.PaymentService;
import com.motosphere.payment.util.SecurityUtils;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This service owns the full lifecycle of a payment attempt, but owns
 * NOTHING about invoices, garages, or customers directly - every fact it
 * needs about those (amount, ownership, garage commission %) comes from
 * motosphere-backend's /internal/invoices/{id} endpoint via CoreServiceClient,
 * fetched fresh on every order-creation and every verification (never
 * cached), so it's always working from the source of truth.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
	private final PaymentRepository paymentRepository;
	private final RazorpayClient razorpayClient;
	private final CoreServiceClient coreServiceClient;

	@Value("${razorpay.key.id}")
	private String razorpayKeyId;

	@Value("${razorpay.key.secret}")
	private String razorpayKeySecret;

	@Override
	public PaymentResponse makePayment(Long invoiceId, PaymentRequest request) {
		InvoiceDetails invoice = loadAndAuthorize(invoiceId);

		if (request.getPaymentMethod() == PaymentMethod.ONLINE
				&& (request.getTransactionId() == null || request.getTransactionId().isBlank()))
			throw new BadRequestException("transactionId is required for ONLINE payments");

		Payment payment = finalizePayment(invoice, request.getPaymentMethod(), request.getTransactionId());
		return toResponse(payment);
	}

	@Override
	public RazorpayOrderResponse createRazorpayOrder(Long invoiceId) {
		InvoiceDetails invoice = loadAndAuthorize(invoiceId);

		long amountInPaise = invoice.getTotalAmount().multiply(BigDecimal.valueOf(100))
				.setScale(0, RoundingMode.HALF_UP).longValueExact();

		try {
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", amountInPaise);
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", invoice.getInvoiceNumber());
			Order order = razorpayClient.orders.create(orderRequest);

			return new RazorpayOrderResponse(order.get("id"), razorpayKeyId, amountInPaise, "INR",
					invoice.getInvoiceId());
		} catch (RazorpayException e) {
			log.error("Failed to create Razorpay order for invoice {}: {}", invoice.getInvoiceNumber(),
					e.getMessage());
			throw new BadRequestException("Could not start the payment - please try again");
		}
	}

	@Override
	public PaymentResponse verifyRazorpayPayment(Long invoiceId, RazorpayVerifyRequest request) {
		InvoiceDetails invoice = loadAndAuthorize(invoiceId);

		JSONObject options = new JSONObject();
		options.put("razorpay_order_id", request.getRazorpayOrderId());
		options.put("razorpay_payment_id", request.getRazorpayPaymentId());
		options.put("razorpay_signature", request.getRazorpaySignature());

		boolean valid;
		try {
			valid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
		} catch (RazorpayException e) {
			log.error("Razorpay signature verification error for invoice {}: {}", invoice.getInvoiceNumber(),
					e.getMessage());
			valid = false;
		}

		if (!valid)
			throw new BadRequestException("Payment verification failed - this payment could not be confirmed");

		Payment payment = finalizePayment(invoice, PaymentMethod.ONLINE, request.getRazorpayPaymentId());
		return toResponse(payment);
	}

	@Override
	public AdminDashboardResponse getAdminSummary() {
		List<Payment> payments = paymentRepository.findAllByOrderByPaymentDateDesc();

		BigDecimal totalCommission = sum(payments, Payment::getCommissionAmount);
		BigDecimal todaysCommission = sum(paymentsToday(payments), Payment::getCommissionAmount);
		BigDecimal totalRevenue = sum(payments, Payment::getAmountPaid);

		List<GarageEarningsResponse> byGarage = groupByGarage(payments);
		List<TransactionResponse> transactions = payments.stream().map(this::toTransactionResponse)
				.collect(Collectors.toList());

		return new AdminDashboardResponse(totalCommission, todaysCommission, totalRevenue, payments.size(), byGarage,
				transactions);
	}

	@Override
	public ManagerDashboardResponse getGarageSummary(Long garageId) {
		List<Payment> payments = paymentRepository.findByGarageIdOrderByPaymentDateDesc(garageId);

		BigDecimal totalEarnings = sum(payments, Payment::getGarageAmount);
		BigDecimal todaysEarnings = sum(paymentsToday(payments), Payment::getGarageAmount);
		String garageName = payments.isEmpty() ? null : payments.get(0).getGarageName();

		List<TransactionResponse> transactions = payments.stream().map(this::toTransactionResponse)
				.collect(Collectors.toList());

		return new ManagerDashboardResponse(garageName, todaysEarnings, totalEarnings, payments.size(),
				transactions);
	}

	// --- helpers ---

	// Fetches the invoice fresh from the core service and enforces the same
	// ownership/duplicate-payment rules the monolith used to enforce locally -
	// just now against data fetched over the network instead of the local DB.
	private InvoiceDetails loadAndAuthorize(Long invoiceId) {
		InvoiceDetails invoice = coreServiceClient.getInvoiceDetails(invoiceId);

		Long currentUserId = SecurityUtils.getCurrentUserId();
		if (!invoice.getOwnerCustomerUserId().equals(currentUserId))
			throw new UnauthorizedActionException("This invoice doesn't belong to you");

		if ("PAID".equals(invoice.getPaymentStatus()))
			throw new BadRequestException("This invoice has already been paid");

		if (paymentRepository.existsByInvoiceId(invoiceId))
			throw new BadRequestException("A payment already exists for this invoice");

		return invoice;
	}

	private Payment finalizePayment(InvoiceDetails invoice, PaymentMethod method, String transactionId) {
		BigDecimal commissionAmount = invoice.getSubtotal()
				.multiply(BigDecimal.valueOf(invoice.getGarageCommissionPercentage()))
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		BigDecimal garageAmount = invoice.getSubtotal().subtract(commissionAmount).setScale(2, RoundingMode.HALF_UP);

		Payment payment = new Payment(invoice.getInvoiceId(), invoice.getInvoiceNumber(), invoice.getGarageId(),
				invoice.getGarageName(), invoice.getCustomerName(), invoice.getVehicleRegistrationNumber(),
				invoice.getTotalAmount(), commissionAmount, garageAmount, method, transactionId);
		paymentRepository.save(payment);

		// best-effort with retries, never blocks/fails this response - see
		// CoreServiceClient.markInvoicePaid javadoc
		coreServiceClient.markInvoicePaid(invoice.getInvoiceId());

		return payment;
	}

	private List<Payment> paymentsToday(List<Payment> payments) {
		LocalDate today = LocalDate.now();
		return payments.stream().filter(p -> p.getPaymentDate().toLocalDate().equals(today))
				.collect(Collectors.toList());
	}

	private BigDecimal sum(List<Payment> payments, java.util.function.Function<Payment, BigDecimal> field) {
		return payments.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private List<GarageEarningsResponse> groupByGarage(List<Payment> payments) {
		Map<Long, List<Payment>> grouped = payments.stream()
				.collect(Collectors.groupingBy(Payment::getGarageId, LinkedHashMap::new, Collectors.toList()));

		return grouped.entrySet().stream()
				.map(entry -> {
					List<Payment> garagePayments = entry.getValue();
					String garageName = garagePayments.get(0).getGarageName();
					return new GarageEarningsResponse(entry.getKey(), garageName, garagePayments.size(),
							sum(garagePayments, Payment::getAmountPaid), sum(garagePayments, Payment::getCommissionAmount),
							sum(garagePayments, Payment::getGarageAmount));
				})
				.sorted(Comparator.comparing(GarageEarningsResponse::getTotalCommission).reversed())
				.collect(Collectors.toList());
	}

	private TransactionResponse toTransactionResponse(Payment payment) {
		return new TransactionResponse(payment.getPaymentId(), payment.getPaymentDate(), payment.getInvoiceNumber(),
				payment.getGarageName(), payment.getCustomerName(), payment.getVehicleRegistrationNumber(),
				payment.getAmountPaid(), payment.getCommissionAmount(), payment.getGarageAmount(),
				payment.getPaymentMethod(), payment.getTransactionId());
	}

	private PaymentResponse toResponse(Payment payment) {
		return new PaymentResponse(payment.getPaymentId(), payment.getAmountPaid(), payment.getCommissionAmount(),
				payment.getGarageAmount(), payment.getPaymentMethod(), payment.getTransactionId(),
				payment.getPaymentStatus(), payment.getPaymentDate());
	}
}
