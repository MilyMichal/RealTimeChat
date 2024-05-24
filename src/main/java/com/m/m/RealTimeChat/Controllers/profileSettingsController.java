package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Services.OnlineUserService;
import com.m.m.RealTimeChat.Services.UserStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profileUpdate")
public class profileSettingsController {

    private final UserStorageService userStorageService;
    private final OnlineUserService onlineUserService;


    public profileSettingsController(UserStorageService userStorageService, OnlineUserService onlineUserService) {
        this.userStorageService = userStorageService;
        this.onlineUserService = onlineUserService;
    }

    @PostMapping
    public ResponseEntity<?> updateUserProfile(RedirectAttributes redirectAttributes, Authentication auth, @RequestParam(required = false) MultipartFile file,
                                            @RequestParam(required = false) String userName,
                                            @RequestParam(required = false) String newPass,
                                            @RequestParam String actualPass) {
        //String IMAGE_FOLDER = "src/main/resources/static/Images/ProfilePictures/" + auth.getName() ;
        String IMAGE_FOLDER = "C:/Users/michal.mily/Desktop/sources";
        Map<String, String> message = new HashMap<>();
        if (userStorageService.confirmPassword(auth.getName(), actualPass)) {
            String pathForDatabase = "";
            if (!file.isEmpty()) {
                try {
                    byte[] bytes = file.getBytes();
                    Path path = Paths.get(IMAGE_FOLDER + "/" + file.getOriginalFilename());
                    Files.createDirectories(path.getParent());
                    System.out.println("PATH DEBUG: " + path);
                    Files.write(path, bytes);
                     /*pathForDatabase = "/Images/ProfilePictures/" + auth.getName() + "/" + file.getOriginalFilename();*/
                    pathForDatabase = "sources/"  + file.getOriginalFilename();
                } catch (IOException e) {
                    message.put("message", e.getMessage());
                    return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
                }
            }


            userStorageService.updateUserInfo(auth.getName(), pathForDatabase, newPass, userName);
            message.put("message", "Profile successfully updated.");
            return new ResponseEntity<>(message, HttpStatus.ACCEPTED);
        }
        message.put("message", "Update wasn't successful");
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

}
