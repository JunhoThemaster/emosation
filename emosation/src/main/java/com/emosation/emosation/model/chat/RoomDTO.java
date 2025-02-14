package com.emosation.emosation.model.chat;


import com.emosation.emosation.model.user.UserDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class RoomDTO {

    private Long id;
    private String roomName;
    private List<UserDTO> roomUser;
//    private List<MessageDTO> messages;
    private LocalDateTime openedAt;


    public RoomDTO(Long id,String  roomName,List<UserDTO> roomUser,LocalDateTime openedAt) {
        this.id = id;
        this.roomName = roomName;
        this.roomUser = roomUser;
        this.openedAt = openedAt;


    }

}
