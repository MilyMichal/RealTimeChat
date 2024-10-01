package com.m.m.RealTimeChat.Repository;

import com.m.m.RealTimeChat.Models.Message;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query(value = "SELECT * FROM (SELECT * FROM messages WHERE send_to = 'public' ORDER BY date DESC LIMIT 15) subquery ORDER BY date ASC", nativeQuery = true)
    List<Message> findLatestPublicMessages();

    @Query(value = "SELECT * FROM messages WHERE send_to = 'public'", nativeQuery = true)
    List<Message> findAllPublicMessages();

    @Query(value = "SELECT * FROM messages WHERE send_to = :to AND sender = :from OR send_to = :from AND sender = :to", nativeQuery = true)
    List<Message> findAllPrivateMessages(@Param("to") String sendTo, @Param("from") String sender);

    @Query(value = "SELECT * FROM (SELECT * FROM messages WHERE send_to = :to AND sender = :from OR send_to = :from AND sender = :to ORDER BY date DESC LIMIT 15) subquery ORDER BY date ASC ", nativeQuery = true)
    List<Message> findLatestPrivateMessages(@Param("to") String sendTo, @Param("from") String sender);

    @Modifying
    @Transactional
    @Query(value = "UPDATE messages SET sender = CASE WHEN sender = :oldNick AND type != 'update-nick' THEN :newNick ELSE sender END, send_to = CASE WHEN send_to = :oldNick AND type != 'update-nick' THEN :newNick ELSE send_to END WHERE sender = :oldNick OR send_to = :oldNick " +
            ";UPDATE online_users SET nickname = :newNick WHERE nickname = :oldNick", nativeQuery = true)
    void updateHistory(@Param("oldNick") String oldNickname, @Param("newNick") String newNickname);


}
