package com.m.m.RealTimeChat.ErrorHandlers;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;

@ControllerAdvice
public class RegisterExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
        public ModelAndView handleNotAllowedNicknameRegistration (MethodArgumentNotValidException ex) {
            ModelAndView modelAndView = new ModelAndView("Register");
            System.out.println("DEBUG VALIDATION: " + ex.getMessage());
            modelAndView.addObject("message", Objects.requireNonNull(Objects.requireNonNull(ex.getFieldError()).getDefaultMessage()));
            return modelAndView;
        }

    }

