package com.andeva.atelier.platform.core.interfaces.rest.resources;

public record UpdateBranchResource(
        String code,
        String name,
        String address,
        String phone
) {
}
