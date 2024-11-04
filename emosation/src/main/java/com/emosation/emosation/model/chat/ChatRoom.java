package com.emosation.emosation.model.chat;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @Column(name = "room_name", nullable = false)
    private String roomName = "채팅방";

    @Column(name = "openedAt")
    private LocalDateTime openedAt = LocalDateTime.now();


    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<RoomInUsers> roomusers = new ArrayList<>();


    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();


}
