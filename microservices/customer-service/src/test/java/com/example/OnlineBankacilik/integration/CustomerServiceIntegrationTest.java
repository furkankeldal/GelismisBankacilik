package com.example.OnlineBankacilik.integration;

import com.example.OnlineBankacilik.dto.CustomerRequestDto;
import com.example.OnlineBankacilik.dto.CustomerResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Customer Service Full Integration Test")
class CustomerServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /customers ardından GET /customers/{id} - Tam uçtan uca akış")
    void createAndGetCustomer_FullFlow() throws Exception {
        // Given - yeni müşteri isteği
        CustomerRequestDto request = new CustomerRequestDto();
        request.setNameSurname("Integration Test User");
        request.setTcKimlikNo("12345678901");
        request.setNumber("5551234567");
        request.setEmail("integration@test.com");

        // When - müşteri oluştur
        MvcResult createResult = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").isNumber())
                .andExpect(jsonPath("$.nameSurname").value("Integration Test User"))
                .andReturn();

        // Oluşan cevaptan id'yi oku
        String responseJson = createResult.getResponse().getContentAsString();
        CustomerResponseDto created = objectMapper.readValue(responseJson, CustomerResponseDto.class);

        assertThat(created.getCustomerId()).isNotNull();

        Long id = created.getCustomerId();

        // Then - GET ile aynı müşteriyi geri al
        mockMvc.perform(get("/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value(id))
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.nameSurname").value("Integration Test User"));
    }
}


