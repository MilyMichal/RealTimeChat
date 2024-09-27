package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Models.User;
import com.m.m.RealTimeChat.Services.UserStorageService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/register")
public class RegistrationController {


    private final UserStorageService userStorageService;

    public RegistrationController(UserStorageService userStorageService) {
        this.userStorageService = userStorageService;
    }

    @GetMapping
    public String registrationPage() {
        return "Register";
    }

    @Validated
    @PostMapping
    public String registerNerUser(Model model, @Validated User user, @RequestParam("retypedPass") String pass) {
        if (pass.equals(user.getPassword())) {
            userStorageService.saveUserToStorage(user);
            model.addAttribute("message", "Registration Succeed!");
            model.addAttribute("registrationSuccess",true);
            return "Register";
        } else {
            model.addAttribute("message", "password doesn't match");
        }
        return "Register";

    }

}
