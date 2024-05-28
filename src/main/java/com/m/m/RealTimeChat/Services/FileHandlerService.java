package com.m.m.RealTimeChat.Services;

import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileHandlerService {

    private final UserStorageService userStorageService;


    public FileHandlerService(UserStorageService userStorageService) {
        this.userStorageService = userStorageService;
    }


    public Map<String, String> saveNewImageToProfile(Authentication auth,
                                                     MultipartFile file,
                                                     String userName,
                                                     String newPass,
                                                     String actualPass) {
        Map<String, String> message = new HashMap<>();

        String IMAGE_FOLDER = "src/main/resources/static/Images/ProfilePictures/" + auth.getName();
        if (userStorageService.confirmPassword(auth.getName(), actualPass)) {
            if (file.isEmpty() && userName.isEmpty() && newPass.isEmpty()) {
                message.put("message", "There is nothing to update");
            } else {
                String pathForDatabase = "";
                if (!file.isEmpty()) {
                    try {
                        byte[] bytes = file.getBytes();
                        Path path = Paths.get(IMAGE_FOLDER + "/" + file.getOriginalFilename());
                        Files.createDirectories(path.getParent());
                        System.out.println("PATH DEBUG: " + path);
                        Files.write(path, bytes);
                        pathForDatabase = "src/main/resources/static/Images/ProfilePictures/" + auth.getName() + "/" + file.getOriginalFilename();

                    } catch (IOException e) {
                        message.put("message", e.getMessage());

                    }

                }
                message.put("message","Profile was successfully updated!");
                userStorageService.updateUserInfo(auth.getName(), pathForDatabase, newPass, userName);
            }
        }

        return message;
    }

    public String loadImage(String name) throws IOException {
        String picURL = userStorageService.getUser(name).getProfilePic();
        Path path = Path.of(picURL);
        System.out.println("PATH DEBUG: " + path);
        if (Files.exists(path)) {
            System.out.println("FOUND DEBUG: " + Files.exists(path));
            return Base64.getEncoder().encodeToString(Files.readAllBytes(path));
        }
        return null;
    }
}

