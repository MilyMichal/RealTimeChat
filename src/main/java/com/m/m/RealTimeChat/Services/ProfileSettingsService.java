package com.m.m.RealTimeChat.Services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
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


    public ProfileSettingsService(UserStorageService userStorageService) {
        this.userStorageService = userStorageService;
    }


    public Map<String, String> saveNewImageToProfile(Authentication auth,
                                                     MultipartFile file,
                                                     String userName,
                                                     String newPass,
                                                     String actualPass) {
        Map<String, String> message = new HashMap<>();
        String IMAGE_FOLDER = "ProfilePic";
        if (userStorageService.confirmPassword(auth.getName(), actualPass)) {
            if (file.isEmpty() && userName.isEmpty() && newPass.isEmpty()) {
                message.put("message", "There is nothing to update");
            } else {
                String pathForDatabase = "";
                if (!file.isEmpty()) {
                    try {
                        byte[] bytes = file.getBytes();
                        Path path = Paths.get(IMAGE_FOLDER, auth.getName(),file.getOriginalFilename());
                        Files.createDirectories(path.getParent());
                        System.out.println("PATH DEBUG: " + path);
                        Files.write(path, bytes);
                        pathForDatabase = path.toString();

                    } catch (IOException e) {
                        message.put("message", e.getMessage());

                    }

                }
                message.put("message", "Profile was successfully updated!");
                userStorageService.updateUserInfo(auth.getName(), pathForDatabase, newPass, userName);
            }
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

