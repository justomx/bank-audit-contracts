package com.justo.bank.template.application.port.in;

/**
 * Input command for the creation use case.
 *
 * <p>It is deliberately distinct from the REST DTO ({@code CreateSampleRequest}): the
 * DTO belongs to the HTTP transport and can change with the API version; the command
 * belongs to the use case. If tomorrow the same use case is triggered by a queue
 * message, it reuses this command without dragging in anything HTTP-related.
 */
public record CreateSampleCommand(String name, String description) {
}
