package com.m.m.RealTimeChat.Services;

import com.m.m.RealTimeChat.Models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserStorageServiceTest {

    @Autowired
    UserStorageService userStorageService;
    Long id  = 3L;
    User user = new User(id,"Azrael","azrael","maillW","user");
    @Test
    void addUser(){



        userStorageService.saveUserToStorage(user);

        assertEquals(3,userStorageService.getUsersList().size());


    }

    @Test
    void removeUserFromStorage() {
        userStorageService.removeUserFromStorage(user);

        assertEquals(0,userStorageService.getUsersList().size());
    }
}