package com.bank.contract.account;

import com.bank.contract.MockitoBeanConfig;
import com.bank.controller.CashController;
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

@ActiveProfiles("test")
@WebFluxTest(CashController.class)
@AutoConfigureStubRunner(
        ids = "com.bank:accounts-service-stubs:+:stubs:8100",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import({AccountsServiceClientImpl.class, MockitoBeanConfig.class, TestConfig.class})
@AutoConfigureMessageVerifier
public class CashServiceAccountsContractTest {

    @Autowired
    private AccountsServiceClientImpl accountsServiceClient;

    @Test
    @DisplayName("Проверка получения баланса")
    void testGetCurrentBalance() {
        StepVerifier.create(accountsServiceClient.getCurrentAccountBalance(3L))
                .expectNext(BigDecimal.valueOf(1000))
                .verifyComplete();
    }

    @Test
    @DisplayName("Проверка обновления баланса")
    void testUpdateRemoteBalance() {
        StepVerifier.create(accountsServiceClient.updateRemoteBalance(BigDecimal.valueOf(1000), 3L))
                .verifyComplete();
    }
}
