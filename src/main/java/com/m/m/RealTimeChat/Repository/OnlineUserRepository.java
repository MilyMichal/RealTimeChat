package com.m.m.RealTimeChat.Repository;

import com.m.m.RealTimeChat.Models.OnlineUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OnlineUserRepository extends JpaRepository<OnlineUser,Long> {

    Optional<OnlineUser> findOnlineUserByNickname(String nickname);

   }
