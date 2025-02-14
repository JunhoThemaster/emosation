package com.emosation.emosation.model.chat;

import com.emosation.emosation.model.user.UserDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class MessageDTO {

    private Long id;
    private Long roomId;
    private String content;
    private UserDTO sender;
    private LocalDateTime sentedAt;

    // 생성자
    public MessageDTO(Long id, String content, UserDTO sender, LocalDateTime sentedAt, Long roomId) {
        this.id = id;
        this.content = content;
        this.sender = sender;// 예시: User 객체에서 이메일만 추출
        this.sentedAt = sentedAt;
        this.roomId = roomId;
    }

}
