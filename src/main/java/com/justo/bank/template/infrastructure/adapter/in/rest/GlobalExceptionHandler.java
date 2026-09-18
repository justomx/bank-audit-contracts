package com.justo.bank.template.infrastructure.adapter.in.rest;

import com.justo.bank.template.domain.exception.SampleNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Translation of exceptions into HTTP responses in RFC 7807 format
 * ({@code application/problem+json}).
 *
 * <p>This is the ONLY place in the service where an exception is converted into an
 * HTTP status code. Controllers carry no business try/catch.
 *
 * <p>{@link ProblemDetail}, which Spring provides out of the box, is used instead of
 * a custom error class. Reason: with 24 services, one error format per service forces
 * every consumer (BFF, apps) to write a different parser.
 *
 * <h3>Security rule</h3>
 * <p>The {@code detail} field returned to the client NEVER carries the raw message of
 * an unexpected exception: a stack trace or a driver message can leak table names,
 * paths, or data. The real detail goes to the log associated with a {@code traceId},
 * and only that identifier is handed to the client for support purposes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final URI TYPE_NOT_FOUND = URI.create("https://errors.justo.mx/not-found");
    private static final URI TYPE_VALIDATION = URI.create("https://errors.justo.mx/validation");
    private static final URI TYPE_INTERNAL = URI.create("https://errors.justo.mx/internal");

    @ExceptionHandler(SampleNotFoundException.class)
    public ProblemDetail handleNotFound(SampleNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(TYPE_NOT_FOUND);
        problem.setTitle("Resource not found");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * {@code @Valid} errors. Returns the detail field by field, which is
     * information from the request itself and therefore safe to expose.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "The request has invalid fields");
        problem.setType(TYPE_VALIDATION);
        problem.setTitle("Validation error");
        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Safety net. Any exception not otherwise handled falls here.
     *
     * <p>The real message is recorded in the log together with a generated
     * {@code traceId}; only that identifier reaches the client.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("Unhandled error [traceId={}]", traceId, ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Reference the traceId when reporting it.");
        problem.setType(TYPE_INTERNAL);
        problem.setTitle("Internal error");
        problem.setProperty("traceId", traceId);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
