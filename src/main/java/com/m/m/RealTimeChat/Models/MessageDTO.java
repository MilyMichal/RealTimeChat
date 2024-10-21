package com.m.m.RealTimeChat.Models;

import lombok.Data;

@Data
public class MessageDTO {

    String content;
    String recipient;
    String type;
    String oldNick;
}
