package com.motosphere.entity;

import java.math.BigDecimal;

import com.motosphere.enums.ItemType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "job_card_items")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "jobCard")
public class JobCardItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long jobCardItemId;

	@Column(nullable = false, length = 150)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ItemType itemType;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	// ALWAYS computed by the backend as quantity * unitPrice - never trust a client-supplied amount
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	// Many JobCardItems belong to one JobCard
	@ManyToOne
	@JoinColumn(name = "job_card_id", nullable = false)
	private JobCard jobCard;

	public JobCardItem(String description, ItemType itemType, Integer quantity, BigDecimal unitPrice) {
		super();
		this.description = description;
		this.itemType = itemType;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

}
