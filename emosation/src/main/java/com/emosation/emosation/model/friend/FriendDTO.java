package com.emosation.emosation.model.friend;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendDTO {

    private Long id;
    private String email;
    private String name;


    public FriendDTO(Long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    @Override
    public String toString() {
        return "FriendDTO{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
