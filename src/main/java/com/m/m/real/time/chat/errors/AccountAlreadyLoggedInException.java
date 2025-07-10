package com.m.m.real.time.chat.errors;

import org.springframework.security.authentication.AccountStatusException;

public class AccountAlreadyLoggedInException extends AccountStatusException {
     public AccountAlreadyLoggedInException(String msg) {
        super(msg);
    }
}
