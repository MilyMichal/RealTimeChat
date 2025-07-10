package com.m.m.real.time.chat.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "online_users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OnlineUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String nickname;

    public OnlineUser(String name ) {
        this.nickname=name;
    }
}
