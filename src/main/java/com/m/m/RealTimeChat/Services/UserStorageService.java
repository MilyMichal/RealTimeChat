package com.m.m.RealTimeChat.Services;

import com.m.m.RealTimeChat.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.autoconfigure.ssl.SslProperties;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.m.m.RealTimeChat.Models.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserStorageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserStorageService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void saveUserToStorage(User user) {
        System.out.println("SAVE USER TO STORAGE DEBUG");
        if (user != null) {
            user.setProfilePic("resources/static/Images/ProfilePictures/defaultPic.jpg");
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            if (user.getUserName().equals("Admin")) {
                user.setRoles("ADMIN");
            } else {
                user.setRoles("user");
            }
            userRepository.save(user);
        }
    }

    public List<User> getUsersList() {
        return userRepository.findAll();
    }

    public void removeUserFromStorage(User user) {
        if (userRepository.exists(Example.of(user))) {
            userRepository.delete(user);
        }
    }

    public void banUser(String user, LocalDateTime banExp) {

        System.out.println("BANNED DEBUG: user " + user + " is BANNED!");
        User banndedUser = userRepository.findUserByUserName(user).orElseThrow(() -> new UsernameNotFoundException("User doesn't exist"));
        banndedUser.setNonBanned(false);
        banndedUser.setBanExpiration(banExp);
        userRepository.save(banndedUser);

    }

    public void unBanUser(String user) {
        User allowedUser = userRepository.findUserByUserName(user).orElseThrow(() -> new UsernameNotFoundException("User doesn't exist"));
        allowedUser.setNonBanned(true);
        allowedUser.setBanExpiration(null);
        userRepository.save(allowedUser);
    }

    public List<User> getBannedUsers() {
        return userRepository.findAllBannedUsers();
    }


    public User getUser(String name) {
        return userRepository.findUserByUserName(name).orElseThrow(() -> new UsernameNotFoundException("Username doesn't exist"));
    }


   /* public String getProfilePic(String name) {
        return userRepository.findProfilePictureByName(name);
    }*/
}

