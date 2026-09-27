package com.motosphere.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "job_cards")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = { "appointment", "mechanic", "items", "invoice" })
public class JobCard {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long jobCardId;

	@Column(length = 1000)
	private String diagnosis;

	@Column(length = 1000)
	private String remarks;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal labourCharge = BigDecimal.ZERO;

	private LocalDate completionDate;

	// JobCard 1-----> 1 Appointment
	@OneToOne
	@JoinColumn(name = "appointment_id", nullable = false, unique = true)
	private Appointment appointment;

	// JobCard *-----> 1 Mechanic (User)
	@ManyToOne
	@JoinColumn(name = "mechanic_id", nullable = false)
	private User mechanic;

	// JobCard 1-----> * JobCardItem
	@OneToMany(mappedBy = "jobCard", cascade = CascadeType.ALL)
	private List<JobCardItem> items = new ArrayList<>();

	// JobCard 1-----> 1 Invoice
	@OneToOne(mappedBy = "jobCard", cascade = CascadeType.ALL)
	private Invoice invoice;

	public JobCard(String diagnosis, String remarks, BigDecimal labourCharge) {
		super();
		this.diagnosis = diagnosis;
		this.remarks = remarks;
		this.labourCharge = labourCharge == null ? BigDecimal.ZERO : labourCharge;
	}

}
