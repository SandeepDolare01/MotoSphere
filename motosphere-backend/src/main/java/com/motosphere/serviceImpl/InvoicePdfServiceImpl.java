package com.motosphere.serviceImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import com.motosphere.entity.Appointment;
import com.motosphere.entity.Invoice;
import com.motosphere.entity.JobCard;
import com.motosphere.entity.JobCardItem;
import com.motosphere.entity.User;
import com.motosphere.entity.Vehicle;
import com.motosphere.service.InvoicePdfService;

import lombok.extern.slf4j.Slf4j;

/**
 * Builds a simple one-page invoice PDF with Apache PDFBox (no HTML/CSS
 * rendering engine needed - just direct positioned text on a content stream).
 * Assumes a job card's item list is short enough to fit one page, which is
 * reasonable for a garage job card; if that ever stops being true this will
 * need pagination.
 */
@Service
@Slf4j
public class InvoicePdfServiceImpl implements InvoicePdfService {

	private static final float MARGIN = 50;
	private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();

	@Override
	public byte[] generatePdf(Invoice invoice) {
		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage(PDRectangle.A4);
			document.addPage(page);

			JobCard jobCard = invoice.getJobCard();
			Appointment appointment = jobCard.getAppointment();
			Vehicle vehicle = appointment.getVehicle();
			User customer = vehicle.getCustomer();
			User mechanic = jobCard.getMechanic();

			try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
				float y = PDRectangle.A4.getHeight() - MARGIN;

				y = writeTitle(cs, y);
				y = writeLine(cs, y, "Invoice Number: " + invoice.getInvoiceNumber(), PDType1Font.HELVETICA_BOLD, 11);
				y = writeLine(cs, y, "Invoice Date: " + invoice.getInvoiceDate(), PDType1Font.HELVETICA, 11);
				y = writeLine(cs, y, "Payment Status: " + invoice.getPaymentStatus(), PDType1Font.HELVETICA, 11);
				y -= 10;

				y = writeLine(cs, y, "Customer: " + customer.getFirstName() + " " + customer.getLastName(),
						PDType1Font.HELVETICA, 11);
				y = writeLine(cs, y, "Vehicle: " + vehicle.getRegistrationNumber(), PDType1Font.HELVETICA, 11);
				y = writeLine(cs, y, "Garage: " + appointment.getGarage().getGarageName(), PDType1Font.HELVETICA, 11);
				y = writeLine(cs, y,
						"Mechanic: " + (mechanic != null ? mechanic.getFirstName() + " " + mechanic.getLastName() : "-"),
						PDType1Font.HELVETICA, 11);
				y -= 10;

				y = writeLine(cs, y, "Diagnosis: " + safe(jobCard.getDiagnosis()), PDType1Font.HELVETICA, 11);
				if (jobCard.getRemarks() != null && !jobCard.getRemarks().isBlank())
					y = writeLine(cs, y, "Remarks: " + jobCard.getRemarks(), PDType1Font.HELVETICA, 11);
				y -= 15;

				y = writeItemsTableHeader(cs, y);
				for (JobCardItem item : jobCard.getItems()) {
					y = writeItemsRow(cs, y, item.getDescription() + " (" + item.getItemType() + ")",
							item.getQuantity(), item.getUnitPrice(), item.getAmount());
				}
				if (jobCard.getLabourCharge() != null
						&& jobCard.getLabourCharge().compareTo(java.math.BigDecimal.ZERO) > 0) {
					y = writeItemsRow(cs, y, "Labour charge", 1, jobCard.getLabourCharge(), jobCard.getLabourCharge());
				}
				y -= 10;

				y = writeRightAlignedLine(cs, y, "Subtotal: Rs. " + invoice.getSubtotal(), PDType1Font.HELVETICA, 11);
				y = writeRightAlignedLine(cs, y,
						"GST (" + invoice.getGstPercentage() + "%): Rs. " + invoice.getGstAmount(),
						PDType1Font.HELVETICA, 11);
				y = writeRightAlignedLine(cs, y, "Total: Rs. " + invoice.getTotalAmount(),
						PDType1Font.HELVETICA_BOLD, 13);

				y -= 30;
				writeLine(cs, y, "Thank you for choosing MotoSphere!", PDType1Font.HELVETICA_OBLIQUE, 10);
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			document.save(out);
			return out.toByteArray();
		} catch (IOException e) {
			log.error("Failed to generate PDF for invoice {}: {}", invoice.getInvoiceNumber(), e.getMessage());
			throw new RuntimeException("Could not generate invoice PDF", e);
		}
	}

	private float writeTitle(PDPageContentStream cs, float y) throws IOException {
		cs.beginText();
		cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
		cs.newLineAtOffset(MARGIN, y);
		cs.showText("MotoSphere - Invoice");
		cs.endText();
		return y - 30;
	}

	private float writeLine(PDPageContentStream cs, float y, String text, PDType1Font font, float size)
			throws IOException {
		cs.beginText();
		cs.setFont(font, size);
		cs.newLineAtOffset(MARGIN, y);
		cs.showText(text);
		cs.endText();
		return y - (size + 6);
	}

	private float writeRightAlignedLine(PDPageContentStream cs, float y, String text, PDType1Font font, float size)
			throws IOException {
		float textWidth = font.getStringWidth(text) / 1000 * size;
		cs.beginText();
		cs.setFont(font, size);
		cs.newLineAtOffset(PAGE_WIDTH - MARGIN - textWidth, y);
		cs.showText(text);
		cs.endText();
		return y - (size + 6);
	}

	private float writeItemsTableHeader(PDPageContentStream cs, float y) throws IOException {
		cs.beginText();
		cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
		cs.newLineAtOffset(MARGIN, y);
		cs.showText(String.format("%-45s %6s %12s %12s", "Item", "Qty", "Unit Price", "Amount"));
		cs.endText();
		return y - 16;
	}

	private float writeItemsRow(PDPageContentStream cs, float y, String description, int quantity,
			java.math.BigDecimal unitPrice, java.math.BigDecimal amount) throws IOException {
		String desc = description.length() > 42 ? description.substring(0, 39) + "..." : description;
		cs.beginText();
		cs.setFont(PDType1Font.HELVETICA, 10);
		cs.newLineAtOffset(MARGIN, y);
		cs.showText(String.format("%-45s %6d %12s %12s", desc, quantity, unitPrice, amount));
		cs.endText();
		return y - 15;
	}

	private String safe(String s) {
		return s == null || s.isBlank() ? "-" : s;
	}
}
