package com.m.m.real.time.chat.controllers;


import com.m.m.real.time.chat.services.PasswordResetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

@Controller
@RequestMapping("/passReset")
public class ResetPassController {

    @Value("${spring.datasource.serverURL}")
    private String serverURL;

    private static final int MIN_PASS_LENGTH = 8;

    private final PasswordResetService passwordResetService;

    public ResetPassController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping
    public String showPasswordResetForm(Model model) {
        model.addAttribute("serverURL", serverURL);
        return "passResetPage";
    }


    @PostMapping
    public ResponseEntity<String> sendResetRequest(@RequestParam String email) throws IOException {
        return passwordResetService.processPassResetRequest(email);
    }

    @GetMapping("/{token}")
    public String proceedPasswordReset(@PathVariable String token, Model model) {
        if (passwordResetService.isOneTimeTokenValid(token)) {
            model.addAttribute("token", token);
            model.addAttribute("serverURL", serverURL);
            return "oneTimeResetFormPage";
        }
        return "expiredTokenPage";
    }

    @PutMapping("/{token}")
    public ResponseEntity<String> submitPasswordChange(@PathVariable String token,
                                                       @RequestParam String newPass,
                                                       @RequestParam String reTypedPass) {
        if (passwordResetService.isOneTimeTokenValid(token)) {

            if(newPass.length() < MIN_PASS_LENGTH ) {
                return new ResponseEntity<>("Password must be at least 8 characters long",HttpStatus.PARTIAL_CONTENT);
            }

            if (Objects.equals(newPass, reTypedPass)) {
                passwordResetService.invalidateOneTimeToken(token);
                return passwordResetService.saveNewPassword(token, newPass);
            }
            return new ResponseEntity<>("New password and re-typed password doesn't match", HttpStatus.PARTIAL_CONTENT);
        }
        return new ResponseEntity<>("Your password reset link is not valid. Pleas send a new password reset request!", HttpStatus.PARTIAL_CONTENT);
    }

}
