package com.emosation.emosation.repository;

import com.emosation.emosation.model.chat.ChatRoom;
import com.emosation.emosation.model.chat.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {


    List<Message> findByChatRoomOrderBySentedAtAsc(ChatRoom chatRoom);

    List<Message> findByChatRoom(ChatRoom chatRoom);
}
