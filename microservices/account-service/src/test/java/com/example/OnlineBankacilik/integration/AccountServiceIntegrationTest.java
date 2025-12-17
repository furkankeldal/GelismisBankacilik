package com.example.OnlineBankacilik.integration;

import com.example.OnlineBankacilik.Kafka.TransactionProducer;
import com.example.OnlineBankacilik.client.CustomerServiceClient;
import com.example.OnlineBankacilik.dto.AccountRequestDto;
import com.example.OnlineBankacilik.dto.AccountResponseDto;
import com.example.OnlineBankacilik.dto.CustomerResponseDto;
import com.example.OnlineBankacilik.dto.TransactionRequestDto;
import com.example.OnlineBankacilik.enums.AccountType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Account Service Full Integration Test")
class AccountServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Dış bağımlılıkları mock'la (gerçek customer-service ve Kafka yok)
    @MockBean
    private CustomerServiceClient customerServiceClient;

    @MockBean
    private TransactionProducer transactionProducer;

    // Kafka yok; consumer bean'i KafkaTemplate ister. Context yüklenirken patlamasın diye mock'luyoruz.
    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    @DisplayName("Hesap aç + para yatır + hesabı getir - Tam uçtan uca akış")
    void openDepositAndGetAccount_FullFlow() throws Exception {
        // Given - müşteri servisi mock'u
        CustomerResponseDto customer = new CustomerResponseDto();
        customer.setCustomerId(1L);
        customer.setNameSurname("Integration Customer");
        customer.setTcKimlikNo("12345678901");
        customer.setEmail("customer@test.com");
        when(customerServiceClient.getCustomerById(1L)).thenReturn(customer);

        // Kafka producer hiçbir şey yapmasın
        doNothing().when(transactionProducer).publish(any());

        // 1) Hesap açma isteği
        AccountRequestDto openRequest = new AccountRequestDto();
        openRequest.setCustomerId(1L);
        openRequest.setAccountType(AccountType.VADESIZ);
        openRequest.setFirstAmount(new BigDecimal("1000.00"));

        MvcResult openResult = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(openRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value(1L))
                .andExpect(jsonPath("$.accountType").value("VADESIZ"))
                .andReturn();

        AccountResponseDto opened =
                objectMapper.readValue(openResult.getResponse().getContentAsString(), AccountResponseDto.class);

        assertThat(opened.getAccountNo()).isNotBlank();
        assertThat(opened.getAmount()).isEqualByComparingTo("1000.00");

        String accountNo = opened.getAccountNo();

        // 2) Para yatırma
        TransactionRequestDto depositRequest = new TransactionRequestDto();
        depositRequest.setAmount(new BigDecimal("500.00"));
        depositRequest.setExplanation("Integration deposit");

        mockMvc.perform(post("/accounts/{accountNo}/deposit", accountNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNo").value(accountNo))
                .andExpect(jsonPath("$.amount").value(1500.00));

        // 3) Hesabı getir ve bakiyeyi doğrula
        MvcResult getResult = mockMvc.perform(get("/accounts/{accountNo}", accountNo))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountNo").value(accountNo))
                .andExpect(jsonPath("$.customerId").value(1L))
                .andReturn();

        AccountResponseDto fetched =
                objectMapper.readValue(getResult.getResponse().getContentAsString(), AccountResponseDto.class);

        assertThat(fetched.getAmount()).isEqualByComparingTo("1500.00");
    }
}


