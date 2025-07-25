package com.m.m.real.time.chat.services;

import com.m.m.real.time.chat.models.OnlineUser;
import com.m.m.real.time.chat.repository.OnlineUserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OnlineUserService {

    private final OnlineUserRepository onlineUserRepository;

    public OnlineUserService(OnlineUserRepository onlineUserRepository) {
        this.onlineUserRepository = onlineUserRepository;

    }

    public List<OnlineUser> getAllOnlineUsers() {
        return onlineUserRepository.findAll();
    }

    public void addOnlineUser(String user) {
        OnlineUser newUser = new OnlineUser(user);
        onlineUserRepository.save(newUser);
    }

    public void removeOnlineUser(String name) {
        OnlineUser user = onlineUserRepository.findOnlineUserByNickname(name)
                .orElseThrow(() -> new UsernameNotFoundException("Not found"));
        onlineUserRepository.delete(user);
    }

    public Optional<OnlineUser> findOnlineUser(String name) {
        return onlineUserRepository.findOnlineUserByNickname(name);
    }
}
