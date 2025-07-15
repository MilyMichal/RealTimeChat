package com.m.m.real.time.chat.error_handlers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;


@ControllerAdvice
public class DatabaseExceptionHandler {


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        String errorMessage = extractErrorMessage(exception);
        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("message", errorMessage);
        return modelAndView;
    }

    private String extractErrorMessage(DataIntegrityViolationException exception) {
        String errorMessage = exception.getMessage();
        if (errorMessage.contains("Key (email)")) {
            return "Email is already registered";
        } else if (errorMessage.contains("Key (user_name)")) {
            return "username already exists";
        } else {
            return "ERROR: " + errorMessage;
        }
    }
}
