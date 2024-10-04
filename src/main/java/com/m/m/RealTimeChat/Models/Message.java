package com.m.m.RealTimeChat.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "Messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 500)
    String content;
    @Column
    String sender;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",shape = JsonFormat.Shape.STRING,timezone = "Europe/Prague")
    ZonedDateTime date;
    @Column
    String type;
    @Column
    String sendTo;
}
