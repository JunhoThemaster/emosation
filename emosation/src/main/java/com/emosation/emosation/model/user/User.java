package com.emosation.emosation.model.user;


import com.emosation.emosation.model.chat.Message;
import com.emosation.emosation.model.chat.RoomInUsers;


import com.emosation.emosation.model.friend.Friends;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name ="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "name",nullable = false)
    private String name;

    @Column(name= "pw",nullable = false)
    private String pw;

    @Column(name = "email",nullable = false,unique = true)
    private String email;

    @Column(name = "phone",nullable = false)
    private BigInteger phone;

    @Column(name = "profile_pic",nullable = false)
    private String pics;

    @Column(name = "isAdmin")
    private Boolean isAdmin = false;

    @Column(name = "status",nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        DELETED
    }

    @Column(name = "registerd_at",nullable = false)
    private LocalDateTime registerd_at = LocalDateTime.now();


    @OneToMany(mappedBy = "user")
    private List<RoomInUsers> myrooms = new ArrayList<>();

    @OneToMany(mappedBy = "sender" ,cascade = CascadeType.ALL)
    private List<Message> sentedmessages = new ArrayList<>();





}
