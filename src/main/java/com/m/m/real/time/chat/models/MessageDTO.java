package com.m.m.real.time.chat.models;

import lombok.Data;

@Data
public class MessageDTO {

    String content;
    String recipient;
    String type;
    String oldNick;
}
