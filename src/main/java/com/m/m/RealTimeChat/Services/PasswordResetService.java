package com.m.m.RealTimeChat.Services;

import com.m.m.RealTimeChat.Models.User;
import com.m.m.RealTimeChat.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class PasswordResetService {

    private final MailService mailService;
    private final UserRepository userRepository;

    public PasswordResetService(MailService mailService, UserRepository userRepository) {
        this.mailService = mailService;
        this.userRepository = userRepository;

    }


    public ResponseEntity<String> processPassResetRequest(String email) throws IOException {
        Optional<User> existingUser = userRepository.findUserByMail(email);
        if (existingUser.isPresent()) {
            mailService.sendEmail(email);
            return new ResponseEntity<>("Reset request was send to your email", HttpStatus.OK);
        }
        return new ResponseEntity<>("There is no user with this email", HttpStatus.ACCEPTED);
    }
}
