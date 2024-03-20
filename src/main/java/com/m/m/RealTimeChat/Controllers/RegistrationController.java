package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Models.User;
import com.m.m.RealTimeChat.Services.UserStorageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @PostMapping
    public String registerNerUser(Model model, User user,@RequestParam("retypedPass") String pass){
        if (pass.equals(user.getPassword())) {
            model.addAttribute("message", userStorageService.saveUserToStorage(user));
        } else {
            model.addAttribute("message","Password doesn't match");
            return "Register";
        }
    return "Register";
    }
}
