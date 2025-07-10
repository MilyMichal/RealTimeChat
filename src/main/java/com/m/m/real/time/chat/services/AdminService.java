package com.m.m.real.time.chat.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDateTime;
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
                return exception.getMessage();
            }
        }
        return "Unknown error";
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkBanExpired() {
        userStorageService.getBannedUsers().forEach(bannedUser -> {
            if (bannedUser.getBanExpiration().isBefore(ChronoLocalDateTime.from(ZonedDateTime.now()))) {
                userStorageService.unBanUser(bannedUser.getUserName());
                Map<String, String> message = new HashMap<>();
                message.put("type", "BanExpired");
                message.put("sendTo", bannedUser.getUserName());
                messagingTemplate.convertAndSend("/queue/public", message);
            }
        });


    }
}