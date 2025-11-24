package com.bank.contract.notification;

import com.bank.contract.MockBeanConfig;
import com.bank.controller.AccountController;
import com.bank.service.NotificationService;
import com.bank.service.NotificationServiceImpl;
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

@WebFluxTest(AccountController.class)
@AutoConfigureStubRunner(
        ids = "com.bank:notification-service-stubs:+:stubs:8100",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import({NotificationServiceImpl.class, MockBeanConfig.class, TestConfig.class})
@ActiveProfiles("test")
@AutoConfigureMessageVerifier
public class AccountsServiceNotificationsContractTest {

    @Autowired
    private NotificationService notificationsService;

    @Test
    @DisplayName("Проверка отправки уведомлений")
    void testNotification() {

        StepVerifier.create(notificationsService.sendNotification("test@test.ru", "Test", "text"))
                .verifyComplete();
    }
}
