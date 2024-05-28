package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Services.FileHandlerService;
import com.m.m.RealTimeChat.Services.UserStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class profileSettingsController {

    private final FileHandlerService fileHandlerService;


    public profileSettingsController(FileHandlerService fileHandlerService) {
        this.fileHandlerService = fileHandlerService;


    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUserProfile(Authentication auth, @RequestParam(required = false) MultipartFile file,
                                               @RequestParam(required = false) String userName,
                                               @RequestParam(required = false) String newPass,
                                               @RequestParam String actualPass) {
        return new ResponseEntity<>(fileHandlerService.saveNewImageToProfile(auth, file, userName, newPass, actualPass), HttpStatus.OK);
    }

    @GetMapping("get/{username}")
    public ResponseEntity<?> getProfilePic(@PathVariable String username) throws IOException {

        if (fileHandlerService.loadImage(username) != null) {

            return new ResponseEntity<>(fileHandlerService.loadImage(username), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

}
