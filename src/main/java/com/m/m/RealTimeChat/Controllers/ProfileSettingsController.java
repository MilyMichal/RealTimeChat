package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Services.ProfileSettingsService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/profile")
public class ProfileSettingsController {

    private final ProfileSettingsService profileSettingsService;


    public ProfileSettingsController(ProfileSettingsService profileSettingsService) {
        this.profileSettingsService = profileSettingsService;


    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUserProfile(Authentication auth, @RequestParam(required = false) MultipartFile file,
                                               @RequestParam(required = false) String userName,
                                               @RequestParam(required = false) String newPass,
                                               @RequestParam String actualPass) {

        return new ResponseEntity<>(profileSettingsService.updateUserProfile(auth, file, userName, newPass, actualPass), HttpStatus.OK);
    }

    @GetMapping("get/{username}")
    public ResponseEntity<Resource> getProfilePic(@PathVariable String username) throws IOException {

        if (profileSettingsService.loadImage(username) != null) {

            return new ResponseEntity<>(profileSettingsService.loadImage(username), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteUserProfile(Authentication auth, @RequestParam String actualPass) {
        return profileSettingsService.deleteUserProfile(auth.getName(), actualPass);
    }
}
