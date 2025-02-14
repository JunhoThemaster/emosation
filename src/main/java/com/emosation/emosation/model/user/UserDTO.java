package com.emosation.emosation.model.user;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
public class UserDTO {

    private Long id;
    private String name;
    private String email;
    private String profilepic;
    private User.UserStatus userStatus;
    private String registrationDate;


    public UserDTO(Long id, String name, String email, String profilepic,String registrationDate,User.UserStatus userStatus) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profilepic = profilepic;
        this.registrationDate = registrationDate;
        this.userStatus = userStatus;
    }


    @Override
    public boolean equals(Object o) { // 채팅방 접속시 보내지는 메세지 receiver배열에 내가 보낸 메세지 갯수만큼 userDTO가 생성되기에 이메일을 기준으로 동일한 사용자인지 판단.
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(email, userDTO.email); // 이메일로 비교
    }

    @Override
    public int hashCode() {
        return Objects.hash(email); // 이메일을 기준으로 해시코드 생성
    }

}
