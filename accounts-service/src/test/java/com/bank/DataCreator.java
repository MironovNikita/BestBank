package com.bank;

import com.bank.dto.account.AccountMainPageDto;
import com.bank.dto.account.AccountPasswordChangeDto;
import com.bank.dto.account.AccountUpdateDto;
import com.bank.dto.account.RegisterAccountRequest;
import com.bank.dto.cash.UpdateBalanceRq;
import com.bank.dto.login.LoginRequest;
import com.bank.dto.transfer.TransferOperationDto;
import com.bank.entity.Account;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public class DataCreator {

    public static RegisterAccountRequest createRegisterRq() {
        RegisterAccountRequest rq = new RegisterAccountRequest();
        rq.setEmail("test@test.ru");
        rq.setPassword("123456");
        rq.setBirthdate(LocalDate.of(1990, 12, 12));
        rq.setName("test");
        rq.setSurname("test");
        rq.setPhone("89995552233");
        return rq;
    }

    public static AccountMainPageDto createAccountMainPageDto(String id) {
        return new AccountMainPageDto(id, "89996669966", "Test", "Test");
    }

    public static AccountPasswordChangeDto createAccountPasswordChangeDto() {
        return new AccountPasswordChangeDto("newPassword", "newPassword");
    }

    public static AccountUpdateDto createAccountUpdateDto() {
        AccountUpdateDto accountUpdateDto = new AccountUpdateDto();
        accountUpdateDto.setEmail("test@test.ru");
        accountUpdateDto.setBirthdate(LocalDate.of(1990, 12, 12));
        accountUpdateDto.setName("test");
        accountUpdateDto.setSurname("test");
        accountUpdateDto.setPhone("89995552233");
        return accountUpdateDto;
    }

    public static LoginRequest createLoginRequest() {
        return new LoginRequest("test@test.ru", "123456");
    }

    public static UpdateBalanceRq createUpdateBalanceRq() {
        return new UpdateBalanceRq(1000L);
    }

    public static TransferOperationDto createTransferOperationDto(Long accIdFrom, Long accIdTo) {
        return new TransferOperationDto(accIdFrom, accIdTo, "test@test.ru", 1000L);
    }

    public static Account createAccount(Long id) {
        return new Account(id, "test@test.ru", "password", "test", "test",
                LocalDate.of(1990, 12, 12), "89996663322", 0L);
    }
}
