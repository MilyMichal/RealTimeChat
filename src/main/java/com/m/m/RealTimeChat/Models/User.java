package com.m.m.RealTimeChat.Models;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 20, unique = true)
    String userName;

    String password;
    @Column(length = 20)
    String nickname;

    @Column(unique = true)
    String email;

    String roles;

    @Column(name = "pictureURL")
    String profilePic;

    boolean isNonBanned = true;

    LocalDateTime banExpiration;


}
