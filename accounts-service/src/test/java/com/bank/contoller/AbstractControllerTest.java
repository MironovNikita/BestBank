package com.bank.contoller;

import com.bank.config.MockSecurityConfig;
import com.bank.controller.AccountController;
import com.bank.controller.UserController;
import com.bank.service.AccountService;
import com.bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@WebFluxTest(controllers = {AccountController.class, UserController.class})
@Import({MockSecurityConfig.class})
public abstract class AbstractControllerTest {

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    protected AccountService accountService;

    @MockitoBean
    protected UserService userService;
}
