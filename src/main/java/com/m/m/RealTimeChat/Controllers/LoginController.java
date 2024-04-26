package com.m.m.RealTimeChat.Controllers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {


    @GetMapping("/login-error")
        public String getLoginPage(HttpServletRequest request, Model model)/*@RequestParam(name = "error",required = false)String error)*/{
        HttpSession session = request.getSession(false);
        String errorMessage = null;
        if (session != null) {
            AuthenticationException exception = (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (exception != null)
            {
            errorMessage = exception.getMessage();
            }
        }
        model.addAttribute("message",errorMessage);
        return "loginPage";
    }

    @GetMapping("/login")
    public String getLoginPage() {
            return "loginPage";
    }
}
