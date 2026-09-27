package com.motosphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// what a SUPER_ADMIN sees when reviewing garage applications - includes the
// applicant manager's contact details, which GarageResponse doesn't expose
@Getter
@Setter
@AllArgsConstructor
public class PendingGarageResponse {
	private Long garageId;
	private String garageName;
	private String ownerName;
	private String address;
	private String contactNumber;
	private String email;
	private String applicantManagerName;
	private String applicantManagerEmail;
}
