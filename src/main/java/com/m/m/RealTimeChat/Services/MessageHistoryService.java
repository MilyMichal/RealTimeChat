package com.m.m.RealTimeChat.Services;

import com.m.m.RealTimeChat.Models.Message;
import com.m.m.RealTimeChat.Repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageHistoryService {

    private final MessageRepository messageRepository;

    public MessageHistoryService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void saveMessage(Message message) {
        if (message != null) {
            messageRepository.save(message);
        }
    }

    public List<Message> getPublicHistory() {
      return   messageRepository.findAllPublicMessages();
    }
}
