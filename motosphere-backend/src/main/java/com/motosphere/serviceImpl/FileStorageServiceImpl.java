package com.motosphere.serviceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.motosphere.exception.BadRequestException;
import com.motosphere.service.FileStorageService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Stores uploaded files on local disk under motosphere.uploads.dir (relative
 * to wherever the app is run from). Good enough for a local/demo deployment;
 * if this ever needs to run on more than one instance or survive redeploys,
 * swap this for an S3-backed implementation behind the same interface -
 * nothing else in the codebase needs to change.
 */
@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

	@Value("${motosphere.uploads.dir}")
	private String uploadsDir;

	private Path storageRoot;

	@PostConstruct
	public void init() {
		storageRoot = Path.of(uploadsDir).toAbsolutePath().normalize();
		try {
			Files.createDirectories(storageRoot);
		} catch (IOException e) {
			throw new RuntimeException("Could not create upload directory: " + storageRoot, e);
		}
	}

	@Override
	public String store(MultipartFile file) {
		if (file == null || file.isEmpty())
			throw new BadRequestException("No file was uploaded");

		String contentType = file.getContentType();
		if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png")
				|| contentType.equals("image/webp")))
			throw new BadRequestException("Only JPEG, PNG, or WEBP images are allowed");

		String extension = switch (contentType) {
			case "image/png" -> ".png";
			case "image/webp" -> ".webp";
			default -> ".jpg";
		};
		String storedFileName = UUID.randomUUID() + extension;

		try {
			Path target = storageRoot.resolve(storedFileName).normalize();
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
			return storedFileName;
		} catch (IOException e) {
			log.error("Failed to store uploaded file: {}", e.getMessage());
			throw new RuntimeException("Could not save the uploaded file", e);
		}
	}

	@Override
	public Resource load(String storedFileName) {
		try {
			Path filePath = storageRoot.resolve(storedFileName).normalize();
			// Guard against a stored/passed-in name ever escaping the upload
			// directory (defense in depth - storedFileName is always a
			// server-generated UUID, never user input, but this keeps it safe
			// even if that assumption is ever broken elsewhere).
			if (!filePath.startsWith(storageRoot))
				throw new BadRequestException("Invalid file reference");

			Resource resource = new UrlResource(filePath.toUri());
			if (!resource.exists() || !resource.isReadable())
				throw new BadRequestException("File not found");
			return resource;
		} catch (java.net.MalformedURLException e) {
			throw new BadRequestException("Invalid file reference");
		}
	}

	@Override
	public void delete(String storedFileName) {
		try {
			Path filePath = storageRoot.resolve(storedFileName).normalize();
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			log.error("Failed to delete stored file {}: {}", storedFileName, e.getMessage());
		}
	}
}
