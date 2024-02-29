package com.m.m.RealTimeChat.Repository;

import com.m.m.RealTimeChat.Models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {
    @Query(value = "SELECT * FROM messages WHERE send_to = 'public'",nativeQuery = true)
    List<Message> findAllPublicMessages();

}
