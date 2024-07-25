package com.m.m.RealTimeChat.Controllers;



import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ServerErrorController implements ErrorController {


    @GetMapping("/error")
    public String errorHandler(Model model, HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        Class<?> exceptionType = (Class<?>) request.getAttribute("jakarta.servlet.error.exception_type");
        String errorMessage = (String) request.getAttribute("jakarta.servlet.error.message");
        Throwable throwable = (Throwable) request.getAttribute("jakarta.servlet.error.exception");

        String servletName = (String) request.getAttribute("jakarta.servlet.error.servlet_name");
        model.addAttribute("errorCode", statusCode);
        model.addAttribute("errorType", exceptionType);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("errorThrowable", throwable);

        return "errorPage";
    }
}
