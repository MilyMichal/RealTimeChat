package com.m.m.RealTimeChat.Services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@EnableScheduling
public class AdminService {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserStorageService userStorageService;

    public AdminService(SimpMessagingTemplate messagingTemplate, UserStorageService userStorageService) {
        this.messagingTemplate = messagingTemplate;
        this.userStorageService = userStorageService;
    }

    public String manageErrorMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            AuthenticationException exception = (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (exception != null) {
                if (exception.getMessage().contains("locked")) {
                    return "Your account is BANNED!";
                }
                return exception.getMessage();

            }
        }
        return "Unknown error";
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkBanExpired() {
        System.out.println("BAN SCHEDULED CHECK");
        userStorageService.getBannedUsers().forEach(bannedUser -> {
            if (bannedUser.getBanExpiration().isBefore(LocalDateTime.now())) {
                userStorageService.unBanUser(bannedUser.getUserName());
                Map<String, String> message = new HashMap<>();
                message.put("type", "BanExpired");
                message.put("sendTo", bannedUser.getUserName());
                messagingTemplate.convertAndSend("/topic/chat",message);
        }
        });


    }
}