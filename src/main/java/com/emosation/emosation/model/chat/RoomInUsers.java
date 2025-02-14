package com.emosation.emosation.model.chat;


import com.emosation.emosation.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "roominusers")
public class RoomInUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="room_id" ,nullable=false)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name="user_id" ,nullable=false)
    private User user;

    @Column(name="joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();



    @Column(name="read_info")
    private BigInteger read = BigInteger.ZERO;
}
