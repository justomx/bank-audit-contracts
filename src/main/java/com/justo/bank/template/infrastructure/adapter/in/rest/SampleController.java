package com.justo.bank.template.infrastructure.adapter.in.rest;

import com.justo.bank.template.application.port.in.CreateSampleUseCase;
import com.justo.bank.template.application.port.in.GetSampleByIdUseCase;
import com.justo.bank.template.domain.model.Sample;
import com.justo.bank.template.infrastructure.adapter.in.rest.dto.CreateSampleRequest;
import com.justo.bank.template.infrastructure.adapter.in.rest.dto.SampleResponse;
import com.justo.bank.template.infrastructure.adapter.in.rest.mapper.SampleRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * HTTP inbound adapter.
 *
 * <p>HARD RULE: depends only on inbound ports ({@link GetSampleByIdUseCase},
 * {@link CreateSampleUseCase}), never on {@code SampleService}. If this class
 * imported the service, the hexagon would be broken and the architecture test
 * would break the build.
 *
 * <p>The controller has no business logic: it translates HTTP into commands, invokes
 * the port, and translates the result back. If a business {@code if} shows up here,
 * it belongs to the application layer.
 */
@RestController
@RequestMapping("/v1/samples")
@Tag(name = "Samples", description = "Sample operations — remove when initializing the repo")
public class SampleController {

    private final GetSampleByIdUseCase getSampleByIdUseCase;
    private final CreateSampleUseCase createSampleUseCase;
    private final SampleRestMapper mapper;

    public SampleController(GetSampleByIdUseCase getSampleByIdUseCase,
                            CreateSampleUseCase createSampleUseCase,
                            SampleRestMapper mapper) {
        this.getSampleByIdUseCase = getSampleByIdUseCase;
        this.createSampleUseCase = createSampleUseCase;
        this.mapper = mapper;
    }

    @Operation(summary = "Get a sample by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sample found"),
            @ApiResponse(responseCode = "404", description = "Sample not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SampleResponse> getById(
            @Parameter(description = "Sample identifier", example = "1")
            @PathVariable Long id) {
        Sample sample = getSampleByIdUseCase.getById(id);
        return ResponseEntity.ok(mapper.toResponse(sample));
    }

    @Operation(summary = "Create a sample")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sample created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<SampleResponse> create(@Valid @RequestBody CreateSampleRequest request) {
        Sample created = createSampleUseCase.create(mapper.toCommand(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(created));
    }
}
