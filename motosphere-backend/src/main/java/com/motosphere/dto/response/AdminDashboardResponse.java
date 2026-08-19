package com.motosphere.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {
	private BigDecimal totalCommissionEarned; // MotoSphere's cut, all-time
	private BigDecimal todaysCommissionEarned; // MotoSphere's cut, payments made today
	private BigDecimal totalRevenue; // all-time, GST included, across every garage
	private long totalTransactionCount;
	private List<GarageEarningsResponse> earningsByGarage;
	private List<TransactionResponse> transactions;
}
