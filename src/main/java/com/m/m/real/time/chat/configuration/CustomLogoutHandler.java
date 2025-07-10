package com.m.m.real.time.chat.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m.m.real.time.chat.models.Message;
import com.m.m.real.time.chat.services.MessageHistoryService;
import com.m.m.real.time.chat.services.OnlineUserService;
import com.m.m.real.time.chat.services.UserStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;


import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class CustomLogoutHandler implements LogoutHandler {

    private final Object lock = new Object();

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private MessageHistoryService messageHistoryService;

    @Autowired
    private UserStorageService userStorageService;
    @Autowired
    ObjectMapper objectMapper;

    private final SimpMessagingTemplate messagingTemplate;

    public CustomLogoutHandler(SimpMessagingTemplate messagingTemplate) {


        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        synchronized (lock) {
              if (authentication != null) {
                String user = userStorageService.getUser(authentication.getName()).getNickname();
                Map<String, String> message = new HashMap<>();
                message.put("type", "Leave");
                message.put("sender", user);
                message.put("sendTo", "public");
                message.put("date", ZonedDateTime.now().format(formatter));
                message.put("content", user + " just left the chatroom");


                try {
                    String json = objectMapper.writeValueAsString(message);
                    Message disconnectMessage = objectMapper.readValue(json, Message.class);
                    messageHistoryService.saveMessage(disconnectMessage);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                messagingTemplate.convertAndSend("/queue/public", message);
                if (onlineUserService.findOnlineUser(user).isPresent()) {
                    onlineUserService.removeOnlineUser(user);
                }

            }

        }
    }
}

