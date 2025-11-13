package com.bank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final WebClient webClient;

    @GetMapping("/")
    public Mono<String> mainRedirect() {
        return Mono.just("main");
    }


}
