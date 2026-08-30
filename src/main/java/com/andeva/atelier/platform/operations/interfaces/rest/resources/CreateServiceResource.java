package com.andeva.atelier.platform.operations.interfaces.rest.resources;

import java.util.UUID;

public record CreateServiceResource(UUID branchId, String name, Double price) {
}
