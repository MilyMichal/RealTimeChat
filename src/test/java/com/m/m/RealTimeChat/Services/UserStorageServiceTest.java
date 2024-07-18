package com.m.m.RealTimeChat.Services;

import com.m.m.RealTimeChat.Models.User;
//import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

//import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
class UserStorageServiceTest {

    @Autowired
    UserStorageService userStorageService;
    Long id  = 3L;
    //User user = new User(id,"Azrael","azrael","maill@admuuin.cz","user",true, LocalDateTime.now());
    /*@Test
    void addUser(){



        userStorageService.saveUserToStorage(user);

        assertEquals(4,userStorageService.getUsersList().size());


    }

    @Test
    void removeUserFromStorage() {
        userStorageService.removeUserFromStorage(user);

        assertEquals(2,userStorageService.getUsersList().size());
    }*/

   /* @Test
    void getAllBannedUsers() {
        assertEquals(2,userStorageService.getBannedUsers().size());

    }*/
}