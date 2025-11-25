package com.bank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class EmailServiceImpl implements EmailService {

    //Подтягиваются из config-service модуля, поэтому стоит @SuppressWarnings
    private final JavaMailSender javaMailSender;
    private final String fromMail;

    public EmailServiceImpl(JavaMailSender javaMailSender, @Value("${spring.mail.username}") String fromMail) {
        this.javaMailSender = javaMailSender;
        this.fromMail = fromMail;
    }

    @Override
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromMail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
        log.info("Письмо на адрес {} успешно отправлено.", to);
    }
}
