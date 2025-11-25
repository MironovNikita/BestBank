package com.bank.controller;

import com.bank.dto.email.EmailNotificationDto;
import com.bank.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/email")
    public Mono<Void> sendNotification(@Validated @RequestBody EmailNotificationDto dto) {
        return Mono.fromRunnable(() ->
                        emailService.sendEmail(dto.getTo(), dto.getSubject(), dto.getText()))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
