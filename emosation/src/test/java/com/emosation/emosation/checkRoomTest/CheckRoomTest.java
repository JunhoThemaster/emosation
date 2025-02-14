package com.emosation.emosation.checkRoomTest;


import com.emosation.emosation.model.chat.ChatRoom;
import com.emosation.emosation.model.chat.RoomDTO;
import com.emosation.emosation.model.chat.RoomInUsers;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.repository.ChatRepository;
import com.emosation.emosation.repository.RoomInUserRepository;
import com.emosation.emosation.repository.UserRepository;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class CheckRoomTest {


    @MockBean
    private ChatRepository chatRepository;

    @MockBean
    private RoomInUserRepository roomInUserRepository;

    @MockBean
    private UserRepository userRepository;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate; // RedisTemplate 주입


    @Test
    public void testCheckRoom() {
        // RedisTemplate 사용
        assertNotNull(redisTemplate); // RedisTemplate이 정상적으로 주입되었는지 확인
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    public void CheckRoomInUser(){
        String senderem = "seojunho22@gmail.com";
        String receiverem = "dinalove@gmail.com";
        BigInteger integer = BigInteger.valueOf(1234556);

        User user1 = new User();
        user1.setId(1L);
        user1.setEmail(senderem);
        user1.setPw("123456");
        user1.setPhone(integer);
        user1.setStatus(User.UserStatus.ACTIVE);
        user1.setName("name1");
        user1.setPics("sdsd");
        user1.setRegisterd_at(LocalDateTime.now());

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail(receiverem);
        user2.setPw("123456");
        user2.setPhone(integer);
        user2.setStatus(User.UserStatus.ACTIVE);
        user2.setName("name2");
        user2.setPics("sdsd");
        user2.setRegisterd_at(LocalDateTime.now());

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(1L);
        chatRoom.setRoomName(receiverem + "-" + senderem);



        RoomInUsers roomInUsers = new RoomInUsers();

        roomInUsers.setUser(user1);
        roomInUsers.setChatRoom(chatRoom);
        roomInUsers.setJoinedAt(LocalDateTime.now());

        RoomInUsers roomInUsers1 = new RoomInUsers();
        roomInUsers1.setUser(user2);
        roomInUsers1.setChatRoom(chatRoom);
        roomInUsers1.setJoinedAt(LocalDateTime.now());

        chatRoom.getRoomusers().add(roomInUsers);
        chatRoom.getRoomusers().add(roomInUsers1);

        String key = "ChatRoom:" + receiverem + ":" + senderem +":";
        Object val = redisTemplate.opsForValue().get(key);

        Long roomId = null;


        if(val != null && val instanceof String){
            roomId = Long.valueOf((String) val);

        }

        if(roomId != null){
           boolean isSenderExist = chatRoom.getRoomusers().stream().anyMatch(roomUser -> roomUser.getUser().getEmail().equals(senderem));
           boolean isReceiverExist = chatRoom.getRoomusers().stream().anyMatch(roomUser -> roomUser.getUser().getEmail().equals(receiverem));
            System.out.println(isSenderExist);
            System.out.println(isReceiverExist);
        }



        System.out.println(roomId);
        Map<String,Object> map = new HashMap<>();




    }





}
