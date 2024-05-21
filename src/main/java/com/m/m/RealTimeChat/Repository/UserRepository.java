package com.m.m.RealTimeChat.Repository;

import com.m.m.RealTimeChat.Models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mapping.model.SpELContext;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByUserName(String username);

    @Query(value ="SELECT * FROM USERS WHERE is_non_banned = false",nativeQuery = true)
    List<User> findAllBannedUsers();

    /*@Query(value = "SELECT us.profilePic AS pic, ous.nickname FROM onlineUsers ous JOIN users us ON ous.nickname = us.userName",nativeQuery = true)
    List<Map<String,Object>> getProfilePic();*/

   /* @Query(value = "SELECT pictureURL FROM users WHERE user_name = :name",nativeQuery = true)
    String findProfilePictureByName(@Param("name")String userName);*/
}
