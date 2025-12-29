package com.example.OnlineBankacilik.integration;

import com.example.OnlineBankacilik.client.AccountServiceClient;
import com.example.OnlineBankacilik.dto.AccountResponseDto;
import com.example.OnlineBankacilik.dto.ProcessRequestDto;
import com.example.OnlineBankacilik.dto.ProcessResponseDto;
import com.example.OnlineBankacilik.dto.TransactionRequestDto;
import com.example.OnlineBankacilik.enums.AccountType;
import com.example.OnlineBankacilik.enums.TransactionType;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@DisplayName("Process Service Full Integration Test")
class ProcessServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Dış bağımlılıkları mock'la (gerçek account-service yok)
    @MockBean
    private AccountServiceClient accountServiceClient;

    @Test
    @DisplayName("Para yatırma akışı - account-service mock'lanmış tam akış")
    void depositFullFlow() throws Exception {
        String accountNo = "1001";

        // Given - account-service mock'u (mevcut hesap durumu)
        AccountResponseDto beforeDeposit = new AccountResponseDto();
        beforeDeposit.setAccountNo(accountNo);
        beforeDeposit.setCustomerId(1L);
        beforeDeposit.setAccountType(AccountType.VADESIZ);
        beforeDeposit.setAmount(new BigDecimal("1000.00"));
        beforeDeposit.setActive(true);

        AccountResponseDto afterDeposit = new AccountResponseDto();
        afterDeposit.setAccountNo(accountNo);
        afterDeposit.setCustomerId(1L);
        afterDeposit.setAccountType(AccountType.VADESIZ);
        afterDeposit.setAmount(new BigDecimal("1500.00"));
        afterDeposit.setActive(true);

        when(accountServiceClient.getAccount(accountNo)).thenReturn(beforeDeposit);
        when(accountServiceClient.deposit(eq(accountNo), any(TransactionRequestDto.class)))
                .thenReturn(afterDeposit);

        // 1) Process isteği
        ProcessRequestDto request = new ProcessRequestDto();
        request.setAccountNo(accountNo);
        request.setAmount(new BigDecimal("500.00"));
        request.setExplanation("Integration deposit");

        MvcResult result = mockMvc.perform(post("/processes/deposit-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.numberOfAccount").value(accountNo))
                .andExpect(jsonPath("$.transactionType").value("YATIRMA"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.previousBalance").value(1000.00))
                .andExpect(jsonPath("$.newBalance").value(1500.00))
                .andReturn();

        ProcessResponseDto response =
                objectMapper.readValue(result.getResponse().getContentAsString(), ProcessResponseDto.class);

        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getNumberOfAccount()).isEqualTo(accountNo);
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.YATIRMA);
        assertThat(response.isSuccesfull()).isTrue();
    }
}


