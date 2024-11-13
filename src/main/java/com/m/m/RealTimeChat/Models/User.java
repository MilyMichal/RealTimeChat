package com.m.m.RealTimeChat.Models;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;

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

    @NotBlank(message = "Username cannot be empty!")
    @Pattern(regexp = "[A-Za-z0-9]{2,}", message = "Username contains not allowed characters (\\, /, :, *, ?, \", <, >, |)")
    @Length(min = 2, max = 20, message = "Nickname must be at least 2 and max 20 characters long")
    @Column(length = 20, unique = true, nullable = false)
    String userName;

    @NotBlank(message = "Password cannot be empty!")
    @Length(min = 8, message = "Password must be at least 8 characters long")
    String password;

    @NotBlank(message = "Nickname cannot be empty!")
    @Length(min = 2, max = 20, message = "Nickname must be at least 2 and max 20 characters long")
    @Pattern(regexp = "[A-Za-z0-9_-]{2,}", message = "Nickname contains not allowed characters (\\, /, :, *, ?, \", <, >, |)")
    @Column(length = 20, nullable = false)
    String nickname;

    @NotBlank(message = "Email cannot be empty!")
    @Email(message = "This is not a valid email address")
    @Column(unique = true)
    String email;

    String roles;

    @Column(name = "pictureURL")
    String profilePic;

    boolean isNonBanned = true;

    LocalDateTime banExpiration;


}
