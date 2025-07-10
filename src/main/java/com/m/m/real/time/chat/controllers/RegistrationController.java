package com.m.m.real.time.chat.controllers;

import com.m.m.real.time.chat.models.User;
import com.m.m.real.time.chat.services.UserStorageService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    private static final String TEMPLATE = "Register";

    private final UserStorageService userStorageService;

    public RegistrationController(UserStorageService userStorageService) {
        this.userStorageService = userStorageService;
    }

    @GetMapping
    public String registrationPage() {
        return TEMPLATE;
    }

    @Validated
    @PostMapping
    public String registerNerUser(Model model, @Validated User user, @RequestParam("retypedPass") String pass) {
        if (pass.equals(user.getPassword())) {
            userStorageService.saveUserToStorage(user);
            model.addAttribute("message", "Registration Succeed!");
            model.addAttribute("registrationSuccess", true);
        } else {
            model.addAttribute("message", "password doesn't match");
        }
        return TEMPLATE;

    }

}
