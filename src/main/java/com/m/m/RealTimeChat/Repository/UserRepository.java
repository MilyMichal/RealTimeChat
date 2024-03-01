package com.m.m.RealTimeChat.Repository;

import com.m.m.RealTimeChat.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findUserByUserName(String username);

}
