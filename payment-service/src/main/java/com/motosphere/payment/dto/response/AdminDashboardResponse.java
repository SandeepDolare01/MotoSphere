package com.motosphere.payment.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {
	private BigDecimal totalCommissionEarned;
	private BigDecimal todaysCommissionEarned;
	private BigDecimal totalRevenue;
	private long totalTransactionCount;
	private List<GarageEarningsResponse> earningsByGarage;
	private List<TransactionResponse> transactions;
}
