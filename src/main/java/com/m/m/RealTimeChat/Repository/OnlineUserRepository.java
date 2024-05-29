package com.m.m.RealTimeChat.Repository;

import com.m.m.RealTimeChat.Models.OnlineUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OnlineUserRepository extends JpaRepository<OnlineUser,Long> {

    Optional<OnlineUser> findOnlineUserByNickname(String nickname);

   /* @Query(value = "Select os.nickname, us.pictureurl FROM online_users os JOIN users us ON os.nickname = us.user_name",nativeQuery = true)
    List<Map<String,String>> getOnlineUsers();*/

}
