package com.emosation.emosation.model.chat;


import com.emosation.emosation.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name="message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "rdms_id", nullable = true)
    private Long rdmsId;


    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "content")
    private String content;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;


    @Column(name = "sented_at")
    private LocalDateTime sentedAt = LocalDateTime.now();






}
