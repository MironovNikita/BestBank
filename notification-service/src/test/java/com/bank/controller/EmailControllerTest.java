package com.bank.controller;

import com.bank.config.MockSecurityConfig;
import com.bank.dto.email.EmailNotificationDto;
import com.bank.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@WebFluxTest(controllers = EmailController.class)
@Import({MockSecurityConfig.class})
public class EmailControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private EmailService emailService;

    @Test
    @DisplayName("Проверка отправки уведомления: email")
    void shouldSendEmail() {
        EmailNotificationDto dto = new EmailNotificationDto(
                "test@test.ru",
                "Subject",
                "Hello world"
        );

        doNothing().when(emailService).sendEmail(dto.getTo(), dto.getSubject(), dto.getText());

        webClient.post()
                .uri("/email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(emailService).sendEmail(dto.getTo(), dto.getSubject(), dto.getText());
    }

    @Test
    @DisplayName("Должен вернуть 400 при невалидном теле запроса")
    void shouldReturnBadRequestOnInvalidDto() {
        EmailNotificationDto dto = new EmailNotificationDto(
                "",
                "Subject",
                "Hello"
        );

        webClient.post()
                .uri("/email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
