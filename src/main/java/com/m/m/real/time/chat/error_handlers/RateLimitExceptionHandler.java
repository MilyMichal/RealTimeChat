package com.m.m.real.time.chat.error_handlers;

import com.m.m.real.time.chat.errors.RateLimitExceededException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;


@ControllerAdvice
public class RateLimitExceptionHandler {

    @MessageExceptionHandler(RateLimitExceededException.class)
    @SendToUser("/queue/errors")
    public String rateLimitExceededHandle(RateLimitExceededException ex) {
        return ex.getMessage();
    }

}
