package com.m.m.RealTimeChat.Repository;

import com.m.m.RealTimeChat.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

}
