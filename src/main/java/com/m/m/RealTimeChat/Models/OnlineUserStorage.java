package com.m.m.RealTimeChat.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class OnlineUserStorage {

    public CopyOnWriteArrayList<String> onlineUsers = new CopyOnWriteArrayList<>();

}
