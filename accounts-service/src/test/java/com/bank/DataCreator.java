package com.bank;

import com.bank.dto.account.*;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.dto.currency.Currency;
import com.bank.dto.login.LoginRequest;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.dto.user.RegisterUserRequest;
import com.bank.dto.user.UserPasswordChangeDto;
import com.bank.dto.user.UserUpdateDto;
import com.bank.entity.Account;
import com.bank.entity.User;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;

@UtilityClass
public class DataCreator {

    public static RegisterUserRequest createRegisterRq() {
        RegisterUserRequest rq = new RegisterUserRequest();
        rq.setEmail("test@test.ru");
        rq.setPassword("123456");
        rq.setBirthdate(LocalDate.of(1990, 12, 12));
        rq.setName("test");
        rq.setSurname("test");
        rq.setPhone("89995552233");
        return rq;
    }

    public static AccountListDto createAccountListDto(Long id, Long ownerId) {
        return new AccountListDto(id, ownerId, "Test account", Currency.RUB, BigDecimal.valueOf(1000));
    }

    public static UserPasswordChangeDto createAccountPasswordChangeDto() {
        return new UserPasswordChangeDto("newPassword", "newPassword");
    }

    public static UserUpdateDto createAccountUpdateDto() {
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setEmail("test@test.ru");
        userUpdateDto.setBirthdate(LocalDate.of(1990, 12, 12));
        userUpdateDto.setName("test");
        userUpdateDto.setSurname("test");
        userUpdateDto.setPhone("89995552233");
        return userUpdateDto;
    }

    public static LoginRequest createLoginRequest() {
        return new LoginRequest("test@test.ru", "123456");
    }

    public static AccountCreateDto createAccountCreateDto() {
        AccountCreateDto dto = new AccountCreateDto();
        dto.setEmail("test@test.ru");
        dto.setTitle("Test account");
        dto.setCurrency(Currency.RUB);
        return dto;
    }

    public static AccountDeleteDto createAccountDeleteDto(Long accountId) {
        AccountDeleteDto dto = new AccountDeleteDto();
        dto.setId(accountId);
        dto.setEmail("test@test.ru");
        dto.setCurrency(Currency.RUB);
        return dto;
    }

    public static AccountEditDto createAccountEditDto(Long accountId) {
        AccountEditDto dto = new AccountEditDto();
        dto.setId(accountId);
        dto.setNewTitle("Test account");
        dto.setEmail("test@test.ru");
        dto.setCurrency(Currency.RUB);
        return dto;
    }

    public static AccountOtherListDto createAccountOtherListDto(Long accountId, Long ownerId) {
        return new AccountOtherListDto(accountId, ownerId, Currency.RUB, "Test", "Test", "89996663322");
    }

    public static UpdateBalanceRq createUpdateBalanceRq() {
        return new UpdateBalanceRq(BigDecimal.valueOf(1000L));
    }

    public static TransferOperationDto createTransferOperationDto(Long accIdFrom, Long accIdTo) {
        return new TransferOperationDto(accIdFrom, Currency.RUB, accIdTo, Currency.EUR, "test@test.ru", BigDecimal.valueOf(1000L), null);
    }

    public static User createUser(Long userId) {
        return new User(userId, "test@test.ru", "password", "Test", "Test",
                LocalDate.of(1990, 12, 12), "89996663322");
    }

    public static Account createAccount(Long id, Long ownerId, Currency currency) {
        return new Account(id, ownerId, "Title", currency, BigDecimal.valueOf(1000));
    }
}
