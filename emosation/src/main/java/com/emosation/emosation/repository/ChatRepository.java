package com.emosation.emosation.repository;


import com.emosation.emosation.model.chat.ChatRoom;
import com.emosation.emosation.model.chat.RoomInUsers;
import com.emosation.emosation.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<ChatRoom,Long> {

    @Query("SELECT ch FROM ChatRoom ch JOIN ch.roomusers ru WHERE ru.user.id IN :userIds GROUP BY ch.id, ch.openedAt, ch.roomName HAVING COUNT(ru.user.id) = :userCount")
    Optional<ChatRoom> findByRoomusers_UserIdIn(@Param("userIds") List<Long> userIds, @Param("userCount") long userCount);

    @Query("SELECT c FROM ChatRoom c JOIN FETCH c.roomusers WHERE c.id = :roomId")
    Optional<ChatRoom> cfindByRoomId(@Param("roomId") Long roomId);


    List<ChatRoom> findByRoomusers(RoomInUsers roomInUsers);


}
