package com.m.m.RealTimeChat.Repository;

import com.m.m.RealTimeChat.Models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {
    @Query(value = "SELECT * FROM messages WHERE send_to = 'public'",nativeQuery = true)
    List<Message> findAllPublicMessages();


    @Query(value = "SELECT * FROM messages WHERE send_to = :to AND sender = :from OR send_to = :from AND sender = :to",nativeQuery = true)
    List<Message> findAllPrivateMessages(@Param("to") String sendTo, @Param("from") String sender);
}
