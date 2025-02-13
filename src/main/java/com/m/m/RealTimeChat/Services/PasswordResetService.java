package com.m.m.RealTimeChat.Services;

import com.m.m.RealTimeChat.Models.User;
import com.m.m.RealTimeChat.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    @Value("${spring.datasource.serverURL}")
    private String serverURL;

    private final MailService mailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Map<String,Boolean> oneTimeTokenMap = new ConcurrentHashMap<>();

    public PasswordResetService(MailService mailService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.mailService = mailService;
        this.userRepository = userRepository;

        this.passwordEncoder = passwordEncoder;
    }


    public ResponseEntity<String> processPassResetRequest(String email) throws IOException {
        Optional<User> existingUser = userRepository.findUserByMail(email);
        if (existingUser.isPresent()) {
            mailService.sendEmail(email,generateOneTimeLink(existingUser.get().getNickname()));
            return new ResponseEntity<>("Reset request was send to your email", HttpStatus.OK);
        }
        return new ResponseEntity<>("There is no user with this email", HttpStatus.ACCEPTED);
    }

    public ResponseEntity<String> saveNewPassword(String nickname, String newPass) {
        Optional<User> updatedUser = userRepository.findUserByNickname(nickname);
        if(updatedUser.isPresent()){
            User user = updatedUser.get();
            user.setPassword(passwordEncoder.encode(newPass));
            userRepository.save(user);
            return new ResponseEntity<>("Password was successfully changed", HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("User with nickname " + nickname + "was not found!",HttpStatus.OK);
    }


    private String generateOneTimeLink(String nick) {
        String oneTimeToken = UUID.randomUUID().toString();
        oneTimeTokenMap.put(oneTimeToken,true);
        return serverURL +"passReset/" + oneTimeToken + "/" + nick;

    }

    public boolean isOneTimeTokenValid(String token) {
       return oneTimeTokenMap.getOrDefault(token,false);
    }

    public void invalidateOneTimeToken(String token) {
        oneTimeTokenMap.put(token,false);
    }


}
