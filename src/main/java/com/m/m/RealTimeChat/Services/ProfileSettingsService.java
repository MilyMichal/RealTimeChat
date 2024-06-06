package com.m.m.RealTimeChat.Services;

import com.m.m.RealTimeChat.Models.SecuredUser;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProfileSettingsService {

    private final UserStorageService userStorageService;
    private final PasswordEncoder passwordEncoder;

    public ProfileSettingsService(UserStorageService userStorageService, PasswordEncoder passwordEncoder) {
        this.userStorageService = userStorageService;
        this.passwordEncoder = passwordEncoder;
    }


    public Map<String, String> updateUserProfile(Authentication auth,
                                                 MultipartFile file,
                                                 String userName,
                                                 String newPass,
                                                 String actualPass) {


        Map<String, String> message = new HashMap<>();
        String IMAGE_FOLDER = "ProfilePic";
        if (userStorageService.confirmPassword(auth.getName(), actualPass, newPass)) {

            if (file.isEmpty() && userName.isEmpty() && newPass.isEmpty()) {
                message.put("message", "There is nothing to update");

            } else {
                String pathForDatabase = "";
                if (!userName.equals(auth.getName())) {
                    if (auth.isAuthenticated()) {
                        System.out.println("DEBUG AUTH BEFORE CHANGE: " + SecurityContextHolder.getContext().getAuthentication().getName());
                        UserDetails currentUserDetails = (UserDetails) auth.getPrincipal();
                        UserDetails updatedUserDetails = User.builder()
                                .username(userName.isEmpty() ? auth.getName() : userName)
                                .password(newPass.isEmpty() ? currentUserDetails.getPassword() : passwordEncoder.encode(newPass))
                                .authorities(currentUserDetails.getAuthorities())
                                .build();

                        if (!userName.isEmpty()) {
                            message.put("newUserName", userName);
                        } else if (!newPass.isEmpty()) {

                            message.put("pass", "changed");
                        }
                        if (!file.isEmpty()) {
                            try {
                                byte[] bytes = file.getBytes();
                                Path path = Paths.get(IMAGE_FOLDER, auth.getName(), file.getOriginalFilename());
                                Files.createDirectories(path.getParent());
                                System.out.println("PATH DEBUG: " + path);
                                Files.write(path, bytes);
                                pathForDatabase = path.toString();

                            } catch (IOException e) {
                                message.put("message", e.getMessage());

                            }
                        }
                        message.put("message", "Profile was successfully updated!");
                        message.forEach((k, v) -> System.out.println("DEBUG MAP: \n Key: " + k + "\n" + "value: " + v));
                        userStorageService.updateUserInfo(auth.getName(), pathForDatabase, newPass, userName);

                        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(updatedUserDetails, auth.getCredentials(), updatedUserDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(newAuthentication);

                        System.out.println("DEBUG AUTH AFTER CHANGE " + SecurityContextHolder.getContext().getAuthentication().getName());
                    }
                } else {
                    message.put("message", "username is not different");
                }
            }

        } else {
            message.put("message", "New password must be different from the current one");
        }
        return message;
    }

    public Resource loadImage(String name) throws IOException {
        String picURL = userStorageService.getUser(name).getProfilePic();
        Path path = Path.of(picURL);
        System.out.println("PATH DEBUG: " + path);
        if (Files.exists(path)) {
            System.out.println("FOUND DEBUG: " + Files.exists(path));
            return new UrlResource(path.toUri());

        }
        return null;
    }
}

