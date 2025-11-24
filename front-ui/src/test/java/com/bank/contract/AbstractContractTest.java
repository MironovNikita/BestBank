package com.bank.contract;

import com.bank.config.MockSecurityConfig;
import com.bank.config.TestConfig;
import com.bank.controller.account.AccountController;
import com.bank.controller.cash.CashController;
import com.bank.controller.main.MainController;
import com.bank.controller.transfers.TransfersController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test")
@WebFluxTest(controllers = {MainController.class, AccountController.class, CashController.class, TransfersController.class})
@AutoConfigureWebTestClient
@Import({MockSecurityConfig.class, TestConfig.class})
@AutoConfigureStubRunner(
        ids = "com.bank:accounts-service-stubs:+:stubs:8100",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
public abstract class AbstractContractTest {

    @Autowired
    protected WebTestClient webTestClient;
}
