package com.justo.bank.template.domain.exception;

/**
 * Domain exception.
 *
 * <p>It does not extend any Spring class and does not know about HTTP status codes.
 * The translation to a 404 with {@code application/problem+json} is done by
 * {@code GlobalExceptionHandler}, which lives in the infrastructure layer.
 */
public class SampleNotFoundException extends RuntimeException {

    public SampleNotFoundException(Long id) {
        super("Sample with id %d not found".formatted(id));
    }
}
