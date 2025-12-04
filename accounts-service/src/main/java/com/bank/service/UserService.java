package com.bank.service;

import com.bank.dto.user.RegisterUserRequest;
import com.bank.dto.user.UserPasswordChangeDto;
import com.bank.dto.user.UserUpdateDto;
import com.bank.dto.login.LoginRequest;
import com.bank.dto.login.LoginResponse;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<Void> register(RegisterUserRequest req);

    Mono<LoginResponse> login(LoginRequest loginRequest);

    Mono<Void> editPassword(Long id, UserPasswordChangeDto passwordChangeDto);

    Mono<Void> editUser(Long id, UserUpdateDto userUpdateDto);

    Mono<Void> deleteUser(Long userId, String email);
}
