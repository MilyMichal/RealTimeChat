package com.m.m.real.time.chat.services;

import com.m.m.real.time.chat.models.Message;
import com.m.m.real.time.chat.models.MessageDTO;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MessageService {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    public Message generateMessage(MessageDTO incomingMsg,String sender) {
        Message propperMessage = new Message();
        propperMessage.setContent(incomingMsg.getContent());
        propperMessage.setSender(incomingMsg.getType().equals("update-nick") ? incomingMsg.getOldNick() : sender);
        propperMessage.setSendTo(incomingMsg.getRecipient());
        propperMessage.setDate(ZonedDateTime.parse(ZonedDateTime.now().format(formatter)));
        propperMessage.setType(incomingMsg.getType());

        return propperMessage;
    }
}
