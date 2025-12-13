package com.bank.controller;

import com.bank.service.BlockerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/blocker")
@RequiredArgsConstructor
public class BlockerController {

    private final BlockerService blockerService;

    @GetMapping("/check")
    public Mono<Boolean> checkOperation() {
        return blockerService.checkOperation();
    }
}
