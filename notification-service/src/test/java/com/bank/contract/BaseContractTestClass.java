package com.bank.contract;

import com.bank.controller.EmailController;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test")
@AutoConfigureMessageVerifier
@WebFluxTest(controllers = EmailController.class)
@Import({MockBeanConfig.class})
public class BaseContractTestClass {

    @Autowired
    private WebTestClient webClient;

    @BeforeEach
    public void setup() {
        RestAssuredWebTestClient.webTestClient(webClient);
    }
}
