package com.m.m.real.time.chat.repository;

import com.m.m.real.time.chat.models.OnlineUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OnlineUserRepository extends JpaRepository<OnlineUser,Long> {

    Optional<OnlineUser> findOnlineUserByNickname(String nickname);

   }
