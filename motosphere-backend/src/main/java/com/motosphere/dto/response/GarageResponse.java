package com.motosphere.dto.response;

import java.time.LocalTime;

import com.motosphere.enums.ApprovalStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GarageResponse {
	private Long garageId;
	private String garageName;
	private String ownerName;
	private String address;
	private String contactNumber;
	private String email;
	private LocalTime openingTime;
	private LocalTime closingTime;
	private Double rating;
	private Double commissionPercentage;
	private ApprovalStatus approvalStatus;
	// null if the garage hasn't uploaded a photo yet - relative URL like
	// /garages/12/image, or null.
	private String imageUrl;
}
