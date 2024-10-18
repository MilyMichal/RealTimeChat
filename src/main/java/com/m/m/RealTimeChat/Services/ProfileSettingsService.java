package com.m.m.RealTimeChat.Services;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ProfileSettingsService {
    String msg = "message";
    String imageFolder = "ProfilePic";
    String databasePath;


    private final UserStorageService userStorageService;
    private final PasswordEncoder passwordEncoder;

    public ProfileSettingsService(UserStorageService userStorageService, PasswordEncoder passwordEncoder) {
        this.userStorageService = userStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<String> deleteUserProfile(String userName, String pass) {
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
                                                 String nickname,
                                                 String newPass,
                                                 String reTypedPass,
                                                 String actualPass) {
        databasePath = userStorageService.getUser(auth.getName()).getProfilePic();

        Map<String, String> message = new HashMap<>();


        if (userStorageService.confirmActualPassword(auth.getName(), actualPass)) {

            if (file.isEmpty() && nickname.isEmpty() && newPass.isEmpty()) {
                message.put(msg, "There is nothing to update");

            } else {
                if (isNicknameAvailable(nickname, auth, message) && isNewPasswordAvailable(newPass, reTypedPass, actualPass, message)
                ) {
                    doProfileChanges(nickname, newPass, file, auth, message);
                }

            }

        }
        return message;
    }

    public String loadImage(String nickname) {
        String picURL = userStorageService.getUserByNickname(nickname).getProfilePic();
        Path path = Path.of(picURL);
        if (Files.exists(path)) {
            return path.toString();
        }
        return null;
    }

    private boolean isNewNicknameTaken(String newNickname) {
        return userStorageService.getAllNicknames().stream().anyMatch(nickname -> nickname.equalsIgnoreCase(newNickname));
    }


    private void doProfileChanges(String newNickname, String newPassword, MultipartFile picture, Authentication auth, Map<String, String> finalMessage) {
        AtomicBoolean renamed = new AtomicBoolean(false);
        if (!newNickname.isEmpty()) {
            changeNickname(newNickname, renamed, finalMessage, auth);
        }

        if (!newPassword.isEmpty()) {
            changeUserPassword(newPassword, auth, finalMessage);
        }

        if (!picture.isEmpty()) {
            changeUserProfilePicture(picture, renamed, newNickname, auth, finalMessage);
        }

        userStorageService.updateUserInfo(auth.getName(), databasePath, newPassword, newNickname);

        finalMessage.put(msg, "Profile was successfully updated!");
    }

    //change user nickname
    private void changeNickname(String newNickname, AtomicBoolean isRenamed, Map<String, String> message, Authentication auth) {
        if (!userStorageService.getUser(auth.getName()).getProfilePic().contains("defaultPic")) {
            File currentFolder = new File(imageFolder + "/" + userStorageService.getUser(auth.getName()).getNickname());
            String currPic = Objects.requireNonNull(currentFolder.listFiles())[0].getName();
            File newFolder = new File(currentFolder.getParent(), newNickname);

            if (currentFolder.exists()) {
                isRenamed.set(currentFolder.renameTo(newFolder));

                Path newPath = Paths.get(newFolder.getPath(), currPic);

                databasePath = newPath.toString();
            }

        }
        message.put("newNickname", newNickname);
    }

    // change password
    private void changeUserPassword(String newPassword, Authentication auth, Map<String, String> finalMessage) {

        UserDetails currentUserDetails = (UserDetails) auth.getPrincipal();
        UserDetails updatedUserDetails = User.builder()
                .username(auth.getName())
                .password(passwordEncoder.encode(newPassword))
                .authorities(currentUserDetails.getAuthorities())
                .build();

        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(updatedUserDetails, auth.getCredentials(), updatedUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);

        finalMessage.put("pass", "changed");
    }

    //change profile picture
    private void changeUserProfilePicture(MultipartFile newPicture, AtomicBoolean isRenamed, String nickname, Authentication auth, Map<String, String> finalMessage/*, String databasePath*/) {
        try {

            byte[] bytes = newPicture.getBytes();
            Path path = Paths.get(imageFolder, isRenamed.get() ? nickname : userStorageService.getUser(auth.getName()).getNickname(), newPicture.getOriginalFilename());
            Files.createDirectories(path.getParent());

            File userDirectory = new File(String.valueOf(path.toFile().getParent()));


            if (userDirectory.exists() && userDirectory.isDirectory()) {
                File[] files = userDirectory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        Files.delete(file.toPath());

                    }
                }
            }

            Files.write(path, bytes);
            databasePath = path.toString();

        } catch (IOException e) {
            finalMessage.put(msg, e.getMessage());

        }
        finalMessage.put("profPic", "changed");
    }


    // verification methods
    private boolean isNicknameAvailable(String newNickname, Authentication auth, Map<String, String> message) {
        if (!newNickname.isEmpty()) {
            if (newNickname.equals(userStorageService.getUser(auth.getName()).getNickname())) {
                message.put(msg, "New nickname must be different from the current one");
                return false;
            }
            if (isNewNicknameTaken(newNickname)) {
                message.put(msg, "Nickname is already taken");
                return false;
            }
        }
        return true;

    }

    private boolean isNewPasswordAvailable(String newPassword, String reTypedPassword, String actualPassword, Map<String, String> message) {
        if (!newPassword.isEmpty()) {

            if (!reTypedPassword.equals(newPassword)) {
                message.put(msg, "Retyped password doesn't match new password");
                return false;
            }
            if (userStorageService.isNewPassNotDifferent(newPassword, actualPassword)) {
                message.put(msg, "new password must be different from actual");
                return false;
            }

            return true;
        }

        return true;
    }

}