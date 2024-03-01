package com.m.m.RealTimeChat.Services;

import com.m.m.RealTimeChat.Repository.UserRepository;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.m.m.RealTimeChat.Models.User;

import java.util.List;

@Service
public class UserStorageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserStorageService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void saveUserToStorage(User user) {
        if (user != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            userRepository.save(user);
        }
    }
    public List<User> getUsersList(){
        return userRepository.findAll();
    }

public void removeUserFromStorage(User user) {
            if(userRepository.exists(Example.of(user))) {
            userRepository.delete(user);
        }
}
}

