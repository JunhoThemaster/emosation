package com.emosation.emosation.model.friend;


import com.emosation.emosation.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "friends")
public class Friends {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "frs_id")
    private Long id;


    @ManyToOne // 추가한 사람 (나)
    @JoinColumn(name = "addedby")
    private User addedby;


    @ManyToOne  // 추가 당하는 사람(other)
    @JoinColumn(name = "fr_id")
    private User youadded;


    @Column(name = "addedat")
    private LocalDateTime addedat = LocalDateTime.now();

}
