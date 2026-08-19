package com.motosphere.serviceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motosphere.dto.response.InternalInvoiceDetailsResponse;
import com.motosphere.dto.response.InvoiceResponse;
import com.motosphere.entity.Appointment;
import com.motosphere.entity.Invoice;
import com.motosphere.enums.PaymentStatus;
import com.motosphere.enums.Role;
import com.motosphere.exception.BadRequestException;
import com.motosphere.exception.ResourceNotFoundException;
import com.motosphere.exception.UnauthorizedActionException;
import com.motosphere.repository.InvoiceRepository;
import com.motosphere.repository.UserRepository;
import com.motosphere.service.EmailService;
import com.motosphere.service.InvoicePdfService;
import com.motosphere.service.InvoiceService;
import com.motosphere.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
	private final InvoiceRepository invoiceRepository;
	private final UserRepository userRepository;
	private final InvoicePdfService invoicePdfService;
	private final EmailService emailService;

	@Override
	public InvoiceResponse getInvoiceForJobCard(Long jobCardId) {
		Invoice invoice = invoiceRepository.findByJobCard_JobCardId(jobCardId)
				.orElseThrow(() -> new ResourceNotFoundException("No invoice found for this job card"));

		checkAccess(invoice);

		return new InvoiceResponse(invoice.getInvoiceId(), invoice.getInvoiceNumber(), invoice.getInvoiceDate(),
				invoice.getSubtotal(), invoice.getGstPercentage(), invoice.getGstAmount(), invoice.getTotalAmount(),
				invoice.getPaymentStatus());
	}

	@Override
	public byte[] getInvoicePdf(Long invoiceId) {
		Invoice invoice = loadAndAuthorize(invoiceId);

		if (invoice.getPaymentStatus() != PaymentStatus.PAID)
			throw new BadRequestException("The invoice PDF is only available after payment is completed");

		return invoicePdfService.generatePdf(invoice);
	}

	@Override
	public String getInvoiceNumber(Long invoiceId) {
		return loadAndAuthorize(invoiceId).getInvoiceNumber();
	}

	@Override
	public InternalInvoiceDetailsResponse getInvoiceDetailsForInternal(Long invoiceId) {
		Invoice invoice = invoiceRepository.findById(invoiceId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid invoiceId!"));
		Appointment appointment = invoice.getJobCard().getAppointment();

		return new InternalInvoiceDetailsResponse(invoice.getInvoiceId(), invoice.getInvoiceNumber(),
				invoice.getSubtotal(), invoice.getTotalAmount(), invoice.getPaymentStatus(),
				appointment.getVehicle().getCustomer().getUserId(),
				appointment.getVehicle().getCustomer().getFirstName() + " "
						+ appointment.getVehicle().getCustomer().getLastName(),
				appointment.getVehicle().getCustomer().getEmail(), appointment.getGarage().getGarageId(),
				appointment.getGarage().getGarageName(), appointment.getGarage().getCommissionPercentage(),
				appointment.getVehicle().getRegistrationNumber());
	}

	@Override
	public void markInvoicePaidInternal(Long invoiceId) {
		Invoice invoice = invoiceRepository.findById(invoiceId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid invoiceId!"));

		if (invoice.getPaymentStatus() == PaymentStatus.PAID)
			return; // idempotent - a retried callback should not double-send the email

		invoice.setPaymentStatus(PaymentStatus.PAID);

		String customerEmail = invoice.getJobCard().getAppointment().getVehicle().getCustomer().getEmail();
		emailService.sendInvoiceEmail(invoice, customerEmail);
	}

	private Invoice loadAndAuthorize(Long invoiceId) {
		Invoice invoice = invoiceRepository.findById(invoiceId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid invoiceId!"));
		checkAccess(invoice);
		return invoice;
	}

	private void checkAccess(Invoice invoice) {
		if (SecurityUtils.currentUserHasRole(Role.SUPER_ADMIN))
			return;

		Long currentUserId = SecurityUtils.getCurrentUserId();
		Appointment appointment = invoice.getJobCard().getAppointment();

		boolean isOwningCustomer = appointment.getVehicle().getCustomer().getUserId().equals(currentUserId);
		boolean isAssignedMechanic = invoice.getJobCard().getMechanic().getUserId().equals(currentUserId);

		boolean managesThisGarage = false;
		if (SecurityUtils.currentUserHasRole(Role.GARAGE_MANAGER)) {
			var manager = userRepository.findById(currentUserId).orElse(null);
			managesThisGarage = manager != null && manager.getGarage() != null
					&& manager.getGarage().getGarageId().equals(appointment.getGarage().getGarageId());
		}

		if (!isOwningCustomer && !isAssignedMechanic && !managesThisGarage)
			throw new UnauthorizedActionException("You don't have access to this invoice");
	}
}
