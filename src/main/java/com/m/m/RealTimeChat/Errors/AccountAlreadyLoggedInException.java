package com.m.m.RealTimeChat.Errors;

import org.springframework.security.authentication.AccountStatusException;

public class AccountAlreadyLoggedInException extends AccountStatusException {
     public AccountAlreadyLoggedInException(String msg) {
        super(msg);
    }
}
