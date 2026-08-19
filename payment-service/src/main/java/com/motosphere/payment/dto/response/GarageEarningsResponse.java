package com.motosphere.payment.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GarageEarningsResponse {
	private Long garageId;
	private String garageName;
	private long transactionCount;
	private BigDecimal totalRevenue;
	private BigDecimal totalCommission;
	private BigDecimal totalGarageAmount;
}
