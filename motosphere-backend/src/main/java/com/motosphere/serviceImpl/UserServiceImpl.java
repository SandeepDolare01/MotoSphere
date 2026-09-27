package com.motosphere.serviceImpl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motosphere.dto.request.AddMechanicRequest;
import com.motosphere.dto.request.CreateStaffRequest;
import com.motosphere.dto.request.UpdateUserRequest;
import com.motosphere.dto.response.ApiResponse;
import com.motosphere.dto.response.UserResponse;
import com.motosphere.entity.Garage;
import com.motosphere.entity.User;
import com.motosphere.enums.Role;
import com.motosphere.exception.BadRequestException;
import com.motosphere.exception.DuplicateResourceException;
import com.motosphere.exception.ResourceNotFoundException;
import com.motosphere.exception.UnauthorizedActionException;
import com.motosphere.repository.GarageRepository;
import com.motosphere.repository.UserRepository;
import com.motosphere.service.UserService;
import com.motosphere.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final GarageRepository garageRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public ApiResponse createStaff(CreateStaffRequest request) {
		if (request.getRole() != Role.GARAGE_MANAGER && request.getRole() != Role.MECHANIC)
			throw new BadRequestException("Staff accounts must be GARAGE_MANAGER or MECHANIC");

		if (userRepository.existsByEmail(request.getEmail()))
			throw new DuplicateResourceException("An account with this email already exists");

		Garage garage = garageRepository.findById(request.getGarageId())
				.orElseThrow(() -> new ResourceNotFoundException("Invalid garageId!"));

		User user = new User();
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setPhoneNumber(request.getPhoneNumber());
		user.setRole(request.getRole());
		user.setActive(true);
		user.setGarage(garage);

		if (request.getRole() == Role.MECHANIC) {
			user.setSpecialization(request.getSpecialization());
			user.setExperienceYears(request.getExperienceYears());
		}

		userRepository.save(user);
		return new ApiResponse(request.getRole() + " account created!", "Success");
	}

	@Override
	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream().map(this::toDto).toList();
	}

	@Override
	public UserResponse getUserById(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid userId!"));

		Long currentUserId = SecurityUtils.getCurrentUserId();
		boolean isSelf = user.getUserId().equals(currentUserId);
		boolean isSuperAdmin = SecurityUtils.currentUserHasRole(Role.SUPER_ADMIN);
		if (!isSelf && !isSuperAdmin)
			throw new UnauthorizedActionException("You can only view your own profile");

		return toDto(user);
	}

	@Override
	public ApiResponse updateOwnProfile(UpdateUserRequest request) {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		User user = userRepository.findById(currentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid user!"));

		if (request.getFirstName() != null)
			user.setFirstName(request.getFirstName());
		if (request.getLastName() != null)
			user.setLastName(request.getLastName());
		if (request.getPhoneNumber() != null)
			user.setPhoneNumber(request.getPhoneNumber());
		if (user.getRole() == Role.MECHANIC) {
			if (request.getSpecialization() != null)
				user.setSpecialization(request.getSpecialization());
			if (request.getExperienceYears() != null)
				user.setExperienceYears(request.getExperienceYears());
		}

		return new ApiResponse("Profile updated!", "Success");
	}

	@Override
	public ApiResponse deleteUser(Long userId) {
		if (!userRepository.existsById(userId))
			throw new ResourceNotFoundException("Invalid userId!");
		userRepository.deleteById(userId);
		return new ApiResponse("User deleted!", "Success");
	}

	@Override
	public ApiResponse addMechanicToMyGarage(AddMechanicRequest request) {
		if (userRepository.existsByEmail(request.getEmail()))
			throw new DuplicateResourceException("An account with this email already exists");

		Long currentUserId = SecurityUtils.getCurrentUserId();
		User manager = userRepository.findById(currentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid user!"));
		if (manager.getGarage() == null)
			throw new BadRequestException("This account isn't tied to a garage");

		User mechanic = new User();
		mechanic.setFirstName(request.getFirstName());
		mechanic.setLastName(request.getLastName());
		mechanic.setEmail(request.getEmail());
		mechanic.setPassword(passwordEncoder.encode(request.getPassword()));
		mechanic.setPhoneNumber(request.getPhoneNumber());
		mechanic.setRole(Role.MECHANIC);
		mechanic.setSpecialization(request.getSpecialization());
		mechanic.setExperienceYears(request.getExperienceYears());
		mechanic.setActive(true);
		mechanic.setGarage(manager.getGarage());

		userRepository.save(mechanic);
		return new ApiResponse("Mechanic added to your garage!", "Success");
	}

	@Override
	public List<UserResponse> getMyGarageMechanics() {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		User manager = userRepository.findById(currentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid user!"));
		if (manager.getGarage() == null)
			throw new BadRequestException("This account isn't tied to a garage");

		return userRepository.findByGarage_GarageIdAndRole(manager.getGarage().getGarageId(), Role.MECHANIC).stream()
				.map(this::toDto).toList();
	}

	@Override
	public ApiResponse deactivateStaff(Long userId) {
		User target = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid userId!"));

		enforceStaffManagementScope(target);

		target.setActive(false);
		return new ApiResponse(target.getFirstName() + "'s account has been deactivated", "Success");
	}

	@Override
	public ApiResponse reactivateStaff(Long userId) {
		User target = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid userId!"));

		enforceStaffManagementScope(target);

		target.setActive(true);
		return new ApiResponse(target.getFirstName() + "'s account has been reactivated", "Success");
	}

	// SUPER_ADMIN may (de)activate anyone. A GARAGE_MANAGER may only (de)activate
	// a MECHANIC belonging to their own garage - never another manager or admin,
	// and never a mechanic at a different garage.
	private void enforceStaffManagementScope(User target) {
		if (SecurityUtils.currentUserHasRole(Role.SUPER_ADMIN))
			return;

		if (target.getRole() != Role.MECHANIC)
			throw new UnauthorizedActionException("You can only manage mechanic accounts");

		Long currentUserId = SecurityUtils.getCurrentUserId();
		User manager = userRepository.findById(currentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid user!"));

		boolean sameGarage = manager.getGarage() != null && target.getGarage() != null
				&& manager.getGarage().getGarageId().equals(target.getGarage().getGarageId());
		if (!sameGarage)
			throw new UnauthorizedActionException("You can only manage mechanics at your own garage");
	}

	private UserResponse toDto(User user) {
		Long garageId = user.getGarage() == null ? null : user.getGarage().getGarageId();
		String garageName = user.getGarage() == null ? null : user.getGarage().getGarageName();
		return new UserResponse(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(),
				user.getPhoneNumber(), user.getRole(), user.getSpecialization(), user.getExperienceYears(),
				user.isActive(), garageId, garageName);
	}

}
