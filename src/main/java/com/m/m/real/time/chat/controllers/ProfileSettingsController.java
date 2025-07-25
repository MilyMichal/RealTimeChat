package com.m.m.real.time.chat.controllers;

import com.m.m.real.time.chat.services.ProfileSettingsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/profile")
public class    ProfileSettingsController {

    private final ProfileSettingsService profileSettingsService;


    public ProfileSettingsController(ProfileSettingsService profileSettingsService) {
        this.profileSettingsService = profileSettingsService;


    }

    @PutMapping("/update")
    public ResponseEntity<Map<String,String>> updateUserProfile(Authentication auth, @RequestParam(required = false) MultipartFile file,
                                                                @RequestParam(required = false) String nickname,
                                                                @RequestParam(required = false) String newPass,
                                                                @RequestParam(required = false) String reTypedPass,
                                                                @RequestParam(required = false) String actualPass) {

        return profileSettingsService.updateUserProfile(auth, file, nickname, newPass, reTypedPass,actualPass);
    }

    @GetMapping("/get")
    public ResponseEntity<String> getProfilePic(@RequestParam String nickname) {

        if (profileSettingsService.loadImage(nickname) != null) {

            return new ResponseEntity<>(profileSettingsService.loadImage(nickname), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUserProfile(Authentication auth, @RequestParam (required = false) String actualPass) {
        return profileSettingsService.deleteUserProfile(auth, actualPass);
    }
}
