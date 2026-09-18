package com.justo.bank.template.infrastructure.adapter.in.rest.dto;

/**
 * HTTP outbound DTO.
 *
 * <p>Deliberately separated from the domain model: it allows exposing fewer fields
 * than the domain has. In a CDE service this separation is mandatory — the domain may
 * hold the PAN, and the response must only carry the last 4 digits.
 */
public record SampleResponse(
        Long id,
        String name,
        String description
) {
}
