package com.bank.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    @DisplayName("Проверка отправки уведомления: email")
    void shouldSendNotificationAsEmail() {

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail("test@test.ru", "subject", "text");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
