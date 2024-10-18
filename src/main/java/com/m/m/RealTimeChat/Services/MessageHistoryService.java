package com.m.m.RealTimeChat.Services;

import com.m.m.RealTimeChat.Models.Message;
import com.m.m.RealTimeChat.Repository.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public List<Message> getFullPublicHistory() {
        return messageRepository.findAllPublicMessages();
    }

    public List<Message> getLatestPublicHistory() {
        return messageRepository.findLatestPublicMessages();
    }

    public List<Message> getFullPrivateHistory(String sendTo, String sender) {
        return messageRepository.findAllPrivateMessages(sendTo, sender);
    }

    public ResponseEntity<List<Message>> getLatestPrivateHistory(String sendTo, String sender) {
        return new ResponseEntity<>(messageRepository.findLatestPrivateMessages(sendTo, sender), HttpStatus.OK);
    }


    public void updateHistory(String oldNick, String newNick) {
        messageRepository.updateHistory(oldNick, newNick);
    }
}
