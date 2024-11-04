package com.emosation.emosation.model.user;


import com.emosation.emosation.model.chat.Message;
import com.emosation.emosation.model.chat.RoomInUsers;
import jakarta.persistence.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
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

    @Column(name = "email",nullable = false)
    private String email;

    @Column(name = "phone",nullable = false)
    private BigInteger phone;

    @Column(name = "profile_pic",nullable = false)
    private String pics;

    @Column(name = "registerd_at",nullable = false)
    private LocalDateTime registerd_at = LocalDateTime.now();


    @OneToMany(mappedBy = "user")
    private List<RoomInUsers> myrooms = new ArrayList<>();

    @OneToMany(mappedBy = "sender" ,cascade = CascadeType.ALL)
    private List<Message> sentedessages = new ArrayList<>();


}
