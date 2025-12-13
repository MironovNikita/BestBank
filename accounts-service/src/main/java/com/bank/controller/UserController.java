package com.bank.controller;

import com.bank.dto.user.RegisterUserRequest;
import com.bank.dto.user.UserPasswordChangeDto;
import com.bank.dto.user.UserUpdateDto;
import com.bank.dto.login.LoginRequest;
import com.bank.dto.login.LoginResponse;
import com.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Mono<Void> register(@RequestBody @Validated RegisterUserRequest registerRequest) {
        return userService.register(registerRequest);
    }

    @PostMapping("/{id}/editPassword")
    public Mono<Void> editPassword(@PathVariable(name = "id") Long id, @Validated @RequestBody UserPasswordChangeDto passwordChangeDto) {
        return userService.editPassword(id, passwordChangeDto);
    }

    @PostMapping("/{id}/editUser")
    public Mono<Void> editUser(@PathVariable("id") Long id, @Validated @RequestBody UserUpdateDto userUpdateDto) {
        return userService.editUser(id, userUpdateDto);
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Validated @RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    @PostMapping("/delete/{id}")
    public Mono<Void> delete(@PathVariable(name = "id") Long userId, @RequestBody String email) {
        return userService.deleteUser(userId, email);
    }
}
