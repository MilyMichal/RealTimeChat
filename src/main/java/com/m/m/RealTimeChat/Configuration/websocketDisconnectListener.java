package com.m.m.RealTimeChat.Configuration;

import com.m.m.RealTimeChat.Models.MessageDTO;
import com.m.m.RealTimeChat.Services.MessageHistoryService;
import com.m.m.RealTimeChat.Services.MessageService;
import com.m.m.RealTimeChat.Services.OnlineUserService;

import com.m.m.RealTimeChat.Services.UserStorageService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

@Component
public class websocketDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {

    private final OnlineUserService onlineUserService;
    private final SimpMessagingTemplate template;
    private final MessageHistoryService messageHistoryService;
    private final MessageService messageService;
    private final UserStorageService userStorageService;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();

    public websocketDisconnectListener(OnlineUserService onlineUserService, SimpMessagingTemplate template, MessageHistoryService messageHistoryService, MessageService messageService, UserStorageService userStorageService) {
        this.onlineUserService = onlineUserService;
        this.template = template;
        this.messageHistoryService = messageHistoryService;
        this.messageService = messageService;
        this.userStorageService = userStorageService;
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        String user = userStorageService.getUser(Objects.requireNonNull(event.getUser()).getName()).getNickname();

        ScheduledFuture<?> task = scheduler.schedule(() -> {
            MessageDTO msg = new MessageDTO();
            msg.setContent(user + " just left the chatroom");
            msg.setType("Leave");
            msg.setRecipient("public");

            onlineUserService.removeOnlineUser(user);
            messageHistoryService.saveMessage(messageService.generateMessage(msg, user));
            SecurityContextHolder.clearContext();
            template.convertAndSend("/queue/public", Map.of("type", "Leave", "sender", user, "sendTo", "public", "date", LocalDateTime.now(), "content", user + " just left the chatroom"));

        }, 5, TimeUnit.SECONDS);
        pending.put(user, task);
    }


    @EventListener
    public void handleConnect(SessionConnectEvent e) {
        String user = Objects.requireNonNull(StompHeaderAccessor.wrap(e.getMessage()).getUser()).getName();
        ScheduledFuture<?> task = pending.remove(user);
        if (task != null) {
            task.cancel(false);
        }
    }
}
