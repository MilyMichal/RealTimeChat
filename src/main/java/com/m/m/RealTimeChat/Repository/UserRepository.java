package com.m.m.RealTimeChat.Repository;

import com.m.m.RealTimeChat.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByUserName(String username);

    @Query(value = "SELECT * FROM USERS WHERE is_non_banned = false", nativeQuery = true)
    List<User> findAllBannedUsers();

    @Query(value = "SELECT user_name FROM users WHERE user_name != :name", nativeQuery = true)
    List<String> findRawUsernameList(@Param("name") String user);

    @Query(value = "SELECT nickname FROM users", nativeQuery = true)
    List<String> findAllNicknames();
}
