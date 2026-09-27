package com.motosphere.service;

import org.springframework.core.io.Resource;

// Not a REST DTO - just a convenient return type for streaming a stored
// image's bytes back out with the right Content-Type header.
public record ImageFile(Resource resource, String contentType) {
}
