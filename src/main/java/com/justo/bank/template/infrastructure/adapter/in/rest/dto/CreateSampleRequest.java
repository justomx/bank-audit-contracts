package com.justo.bank.template.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * HTTP inbound DTO.
 *
 * <p>Lives in infrastructure, not in domain: it is the API contract and evolves
 * with the endpoint version. Validation annotations belong here precisely because
 * the domain must not carry annotations.
 */
public record CreateSampleRequest(
        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must not exceed 120 characters")
        String name,

        @Size(max = 500, message = "description must not exceed 500 characters")
        String description
) {
}
