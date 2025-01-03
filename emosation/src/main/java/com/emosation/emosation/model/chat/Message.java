package com.emosation.emosation.model.chat;


import com.emosation.emosation.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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


    @Column(name = "read", nullable = true )
    private Boolean read = false;





}
