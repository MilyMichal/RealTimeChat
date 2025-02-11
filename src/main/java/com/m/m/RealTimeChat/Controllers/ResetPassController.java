package com.m.m.RealTimeChat.Controllers;


import com.m.m.RealTimeChat.Services.PasswordResetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/passReset")
public class ResetPassController {

    @Value("${spring.datasource.serverURL}")
    private String serverURL;

    private final PasswordResetService passwordResetService;

    public ResetPassController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping
    public String showPasswordResetForm(Model model){
        model.addAttribute("serverURL", serverURL);
        return "PassResetPage";
    }


    @PostMapping
    public ResponseEntity<String> sendResetRequest(@RequestParam String email) throws IOException {
        return passwordResetService.processPassResetRequest(email);
    }

}
