package com.emosation.emosation.model.user;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private Long id;
    private String name;
    private String email;
    private String profilepic;


    public UserDTO(Long id, String name, String email, String profilepic) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profilepic = profilepic;
    }

}
