package com.bank.contract.account;

import com.bank.contract.MockBeanConfig;
import com.bank.controller.TransfersController;
import com.bank.dto.currency.Currency;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.service.AccountsServiceClientImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@WebFluxTest(TransfersController.class)
@AutoConfigureStubRunner(
        ids = "com.bank:accounts-service-stubs:+:stubs:8100",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import({AccountsServiceClientImpl.class, MockBeanConfig.class, TestConfig.class})
@ActiveProfiles("test")
@AutoConfigureMessageVerifier
public class TransfersServiceAccountsContractTest {

    @Autowired
    private AccountsServiceClientImpl accountsServiceClient;

    @Test
    @DisplayName("Проверка вызова операции перевода")
    void testTransfer() {
        TransferOperationDto dto =
                new TransferOperationDto(3L, Currency.RUB, 2L, Currency.EUR, "test@test.ru", BigDecimal.valueOf(1000), null);

        StepVerifier.create(accountsServiceClient.transfer(dto))
                .verifyComplete();
    }
}
