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
    @Query(value = "UPDATE messages SET sender = CASE WHEN sender = :oldName AND type != 'update-name' THEN :newName ELSE sender END, send_to = CASE WHEN send_to = :oldName AND type != 'update-name' THEN :newName ELSE send_to END WHERE sender = :oldName OR send_to = :oldName " +
            ";UPDATE online_users SET nickname = :newName WHERE nickname = :oldName",nativeQuery = true)
    void updateHistory (@Param("oldName") String oldName, @Param("newName") String newName);


}
