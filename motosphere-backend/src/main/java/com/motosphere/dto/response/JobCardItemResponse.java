package com.motosphere.dto.response;

import java.math.BigDecimal;

import com.motosphere.enums.ItemType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JobCardItemResponse {
	private Long jobCardItemId;
	private String description;
	private ItemType itemType;
	private Integer quantity;
	private BigDecimal unitPrice;
	private BigDecimal amount;
}
