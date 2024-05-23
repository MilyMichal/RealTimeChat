package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Services.OnlineUserService;
import com.m.m.RealTimeChat.Services.UserStorageService;
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

@Controller
@RequestMapping("/profileUpdate")
public class profileSettingsController {

    private final UserStorageService userStorageService;
    private final OnlineUserService onlineUserService;

    private final String IMAGE_FOLDER = "src/main/resources/static/Images/ProfilePictures/";
    public profileSettingsController(UserStorageService userStorageService, OnlineUserService onlineUserService) {
        this.userStorageService = userStorageService;
        this.onlineUserService = onlineUserService;
    }

    @PostMapping
    public String updateUserProfile(RedirectAttributes redirectAttributes, Authentication auth, @RequestParam(required = false) MultipartFile file,
                                  @RequestParam(required = false) String userName,
                                  @RequestParam(required = false) String newPass,
                                  @RequestParam String actualPass) {
        if(userStorageService.confirmPassword(auth.getName(),actualPass)) {
            String finalPath="";
            if(!file.isEmpty()){
                try {
                    byte[] bytes = file.getBytes();
                    Path path = Paths.get(IMAGE_FOLDER + file.getOriginalFilename());
                    Files.write(path,bytes);
                    finalPath = path.toString();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }


            userStorageService.updateUserInfo(auth.getName(),finalPath,newPass,userName);
            redirectAttributes.addFlashAttribute("uploadMessage","Profile was successfully updated! ");
        } else {
            redirectAttributes.addFlashAttribute("uploadMessage","Wrong actual password ");
                    }

        return "redirect:/chat";
    }

}
