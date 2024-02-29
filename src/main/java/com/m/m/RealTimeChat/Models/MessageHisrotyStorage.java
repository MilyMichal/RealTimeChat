package com.m.m.RealTimeChat.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.messaging.handler.MessagingAdviceBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
@Component
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageHisrotyStorage {

    protected List<Message> messageHistory = new CopyOnWriteArrayList<>();

    public void saveMessage(Message message) {
        messageHistory.add(message);
    }
}
