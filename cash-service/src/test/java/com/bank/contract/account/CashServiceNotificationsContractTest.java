package com.bank.contract.account;

import com.bank.contract.MockitoBeanConfig;
import com.bank.controller.CashController;
import com.bank.service.NotificationsServiceClient;
import com.bank.service.NotificationsServiceClientImpl;
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

@WebFluxTest(CashController.class)
@AutoConfigureStubRunner(
        ids = "com.bank:notification-service-stubs:+:stubs:8100",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import({NotificationsServiceClientImpl.class, MockitoBeanConfig.class, TestConfig.class})
@ActiveProfiles("test")
@AutoConfigureMessageVerifier
public class CashServiceNotificationsContractTest {

    @Autowired
    private NotificationsServiceClient notificationsServiceClient;

    @Test
    @DisplayName("Проверка отправки уведомлений")
    void testNotification() {

        StepVerifier.create(notificationsServiceClient.sendNotification("test@test.ru", "Test", "text"))
                .verifyComplete();
    }
}
