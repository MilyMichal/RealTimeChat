package com.m.m.RealTimeChat.Models;

import jakarta.annotation.Nonnull;
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

    @Column(unique = true)
    String userName;

    @Column
    String password;

    @Column(unique = true)
    String email;

    @Column
    String roles;

    boolean isNonBanned = true;

    LocalDateTime banExpiration;


}
