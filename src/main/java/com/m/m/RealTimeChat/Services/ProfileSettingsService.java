package com.m.m.RealTimeChat.Services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ProfileSettingsService {

    private final UserStorageService userStorageService;
    private final PasswordEncoder passwordEncoder;

    public ProfileSettingsService(UserStorageService userStorageService, PasswordEncoder passwordEncoder) {
        this.userStorageService = userStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> deleteUserProfile(String userName, String pass) {
        if (!userStorageService.confirmActualPassword(userName, pass)) {
            return new ResponseEntity<>("incorrect password", HttpStatus.BAD_REQUEST);
        }
        if (userStorageService.removeUserFromStorage(userName)) {
            return new ResponseEntity<>("Your profile was deleted!", HttpStatus.OK);
        }
        return new ResponseEntity<>("Profile cannot be deleted", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    public Map<String, String> updateUserProfile(Authentication auth,
                                                 MultipartFile file,
                                                 String userName,
                                                 String newPass,
                                                 String actualPass) {


        Map<String, String> message = new HashMap<>();
        String IMAGE_FOLDER = "ProfilePic";
        boolean renamed = false;
        if (userStorageService.confirmActualPassword(auth.getName(), actualPass)) {

            if (file.isEmpty() && userName.isEmpty() && newPass.isEmpty()) {
                message.put("message", "There is nothing to update");

            } else {
                String pathForDatabase = userStorageService.getUser(auth.getName()).getProfilePic();

                if (!userName.equals(auth.getName())) {
                    if (isNewUsernameTaken(userName)) {
                        message.put("message", "Username is already taken");
                    } else {
                        if (!newPass.isEmpty() && userStorageService.isNewPassNotDifferent(newPass, actualPass)) {
                            message.put("message", "new password must be different from actual");
                        } else {
                            if (!newPass.isEmpty()) {
                                message.put("pass", "changed");
                            }
                            if (auth.isAuthenticated()) {

                                UserDetails currentUserDetails = (UserDetails) auth.getPrincipal();
                                UserDetails updatedUserDetails = User.builder()
                                        .username(userName.isEmpty() ? auth.getName() : userName)
                                        .password(newPass.isEmpty() ? currentUserDetails.getPassword() : passwordEncoder.encode(newPass))
                                        .authorities(currentUserDetails.getAuthorities())
                                        .build();

                                if (!userName.isEmpty()) {
                                    File currentFolder = new File(IMAGE_FOLDER + "/" + auth.getName());
                                    String currPic = Objects.requireNonNull(currentFolder.listFiles())[0].getName();
                                    File newFolder = new File(currentFolder.getParent(), userName);
                                    if (currentFolder.exists()) {
                                        renamed = currentFolder.renameTo(newFolder);

                                        Path newPath = Paths.get(newFolder.getPath(), currPic);

                                        pathForDatabase = newPath.toString();
                                    }

                                    message.put("newUserName", userName);
                                }

                                if (!file.isEmpty()) {
                                    try {

                                        byte[] bytes = file.getBytes();
                                        Path path = Paths.get(IMAGE_FOLDER, renamed ? userName : auth.getName(), file.getOriginalFilename());
                                        Files.createDirectories(path.getParent());

                                        File userDirectory = new File(String.valueOf(path.toFile().getParent()));


                                        if (userDirectory.exists() && userDirectory.isDirectory()) {
                                            File[] files = userDirectory.listFiles();
                                            if (files != null) {
                                                for (File picture : files) {

                                                    if (!picture.delete()) {
                                                        throw new FileSystemException(picture.getName() + " cannot be deleted!");
                                                    }
                                                }
                                            }
                                        }

                                        Files.write(path, bytes);
                                        pathForDatabase = path.toString();

                                    } catch (IOException e) {
                                        message.put("message", e.getMessage());

                                    }
                                    message.put("profPic", "changed");
                                }
                                message.put("message", "Profile was successfully updated!");
                                message.forEach((k, v) -> System.out.println("DEBUG MAP: \n Key: " + k + "\n" + "value: " + v));
                                userStorageService.updateUserInfo(auth.getName(), pathForDatabase, newPass, userName);

                                Authentication newAuthentication = new UsernamePasswordAuthenticationToken(updatedUserDetails, auth.getCredentials(), updatedUserDetails.getAuthorities());
                                SecurityContextHolder.getContext().setAuthentication(newAuthentication);

                            }
                        }
                    }
                } else {
                    message.put("message", "New username must be different from the current one");
                }
            }
        } else {
            message.put("message","Incorrect password");
        }
        return message;
    }

    public Resource loadImage(String name) throws IOException {
        String picURL = userStorageService.getUser(name).getProfilePic();
        Path path = Path.of(picURL);
        /*System.out.println("PATH DEBUG: " + path);*/
        if (Files.exists(path)) {
            /*System.out.println("FOUND DEBUG: " + Files.exists(path));*/
            return new UrlResource(path.toUri());

        }
        return null;
    }

    private boolean isNewUsernameTaken(String newUsername) {
        return userStorageService.getAllUsers().stream().anyMatch(name -> name.equalsIgnoreCase(newUsername));
    }
}


