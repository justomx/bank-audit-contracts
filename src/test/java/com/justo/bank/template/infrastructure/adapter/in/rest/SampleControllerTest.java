package com.justo.bank.template.infrastructure.adapter.in.rest;

import com.justo.bank.template.application.port.in.CreateSampleCommand;
import com.justo.bank.template.application.port.in.CreateSampleUseCase;
import com.justo.bank.template.application.port.in.GetSampleByIdUseCase;
import com.justo.bank.template.domain.exception.SampleNotFoundException;
import com.justo.bank.template.domain.model.Sample;
import com.justo.bank.template.infrastructure.adapter.in.rest.mapper.SampleRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test of the REST adapter with {@code @WebMvcTest}: starts only the web layer,
 * with the inbound ports mocked. It does not touch the database or Redis.
 *
 * <p>It also verifies that errors come out in RFC 7807 format
 * ({@code application/problem+json}), which is the error contract shared by
 * all services in the vertical.
 */
@WebMvcTest(SampleController.class)
@Import({SampleRestMapper.class, GlobalExceptionHandler.class})
@DisplayName("SampleController")
class SampleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetSampleByIdUseCase getSampleByIdUseCase;

    @MockitoBean
    private CreateSampleUseCase createSampleUseCase;

    @Test
    @DisplayName("GET returns 200 with the sample")
    void getReturns200() throws Exception {
        when(getSampleByIdUseCase.getById(1L)).thenReturn(new Sample(1L, "one", "desc"));

        mockMvc.perform(get("/v1/samples/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("one"));
    }

    @Test
    @DisplayName("GET for an unknown id returns 404 in problem+json")
    void getUnknownIdReturns404ProblemJson() throws Exception {
        when(getSampleByIdUseCase.getById(99L)).thenThrow(new SampleNotFoundException(99L));

        mockMvc.perform(get("/v1/samples/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Valid POST returns 201 with Location header")
    void postValidReturns201() throws Exception {
        when(createSampleUseCase.create(any(CreateSampleCommand.class)))
                .thenReturn(new Sample(5L, "new", "desc"));

        mockMvc.perform(post("/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"new\",\"description\":\"desc\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("POST with an empty name returns 400 with the per-field detail")
    void postInvalidReturns400WithDetail() throws Exception {
        mockMvc.perform(post("/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"desc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.name").exists());
    }
}
