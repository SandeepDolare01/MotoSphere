package com.motosphere.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
	// Saves the file under a randomly generated name and returns that
	// generated name (never the original filename - avoids collisions and
	// path-traversal entirely).
	String store(MultipartFile file);

	Resource load(String storedFileName);

	void delete(String storedFileName);
}
