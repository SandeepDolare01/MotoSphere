package com.motosphere.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.motosphere.entity.Invoice;
import com.motosphere.entity.JobCard;
import com.motosphere.service.EmailService;
import com.motosphere.service.InvoicePdfService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends the invoice as a PDF attachment once a payment succeeds, using
 * Spring's JavaMailSender (SMTP, configured via spring.mail.* properties).
 *
 * A failed email send is logged but never rethrown - by the time this is
 * called the payment has already succeeded and been persisted, so an SMTP
 * hiccup should not turn into a 500 for the customer. It becomes a support
 * follow-up ("resend my invoice"), not a broken payment. The customer can
 * always re-download the PDF from the app regardless of whether the email
 * actually went through.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
	private final JavaMailSender mailSender;
	private final InvoicePdfService invoicePdfService;

	@Value("${mail.from}")
	private String fromAddress;

	@Override
	public void sendInvoiceEmail(Invoice invoice, String toEmail) {
		if (toEmail == null || toEmail.isBlank()) {
			log.warn("Skipping invoice email for invoice {} - no customer email on file", invoice.getInvoiceNumber());
			return;
		}
		try {
			byte[] pdf = invoicePdfService.generatePdf(invoice);

			MimeMessage mimeMessage = mailSender.createMimeMessage();
			// true = multipart, so we can attach the PDF alongside the text body
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setFrom(fromAddress);
			helper.setTo(toEmail);
			helper.setSubject("Your MotoSphere invoice " + invoice.getInvoiceNumber());
			helper.setText(buildBody(invoice));
			helper.addAttachment(invoice.getInvoiceNumber() + ".pdf",
					new org.springframework.core.io.ByteArrayResource(pdf));

			mailSender.send(mimeMessage);
		} catch (Exception e) {
			log.error("Failed to send invoice email for invoice {}: {}", invoice.getInvoiceNumber(), e.getMessage());
		}
	}

	private String buildBody(Invoice invoice) {
		JobCard jobCard = invoice.getJobCard();
		StringBuilder sb = new StringBuilder();
		sb.append("Hi,\n\n");
		sb.append("Thank you for your payment. Your invoice is attached as a PDF.\n\n");
		sb.append("Invoice Number : ").append(invoice.getInvoiceNumber()).append("\n");
		sb.append("Invoice Date   : ").append(invoice.getInvoiceDate()).append("\n");
		sb.append("Vehicle        : ")
				.append(jobCard.getAppointment().getVehicle().getRegistrationNumber()).append("\n");
		sb.append("Total Paid     : Rs. ").append(invoice.getTotalAmount()).append("\n\n");
		sb.append("Thanks for choosing MotoSphere!\n");
		return sb.toString();
	}
}
