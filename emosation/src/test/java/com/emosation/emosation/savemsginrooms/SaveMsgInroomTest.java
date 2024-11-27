package com.emosation.emosation.savemsginrooms;


import com.emosation.emosation.model.chat.ChatRoom;
import com.emosation.emosation.model.chat.Message;
import com.emosation.emosation.model.chat.RoomInUsers;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.repository.ChatRepository;
import com.emosation.emosation.repository.MessageRepository;
import com.emosation.emosation.repository.RoomInUserRepository;
import com.emosation.emosation.repository.UserRepository;
import com.emosation.emosation.sevices.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class SaveMsgInroomTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository msgRepository;

    @Mock
    private RoomInUserRepository roomInUserRepository;




    @Mock
    private UserRepository userRepository; // 유저 저장소
    @Mock
    private User user1;  // user1 설정
    @Mock
    private User user2;  // user2 설정


    @Mock
    private Message message;

    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        // 사용자 설정
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setName("User One");
        user1.setPw("password1");  // 필수 값 설정
        user1.setPhone(new BigInteger("1234567890"));  // 필수 값 설정
        user1.setPics("profilePic1.jpg");  // 필수 값 설정
        user1.setRegisterd_at(LocalDateTime.now());  // 필수 값 설정

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setName("User Two");
        user2.setPw("password2");  // 필수 값 설정
        user2.setPhone(new BigInteger("0987654321"));  // 필수 값 설정
        user2.setPics("profilePic2.jpg");  // 필수 값 설정
        user2.setRegisterd_at(LocalDateTime.now());  // 필수 값 설정

        // userRepository에서 findById가 호출되면 mock으로 설정된 user1과 user2 반환
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));  // User 객체 반환
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

    }





    @Test
    public void testSaveMsgInRoom() {
        String messageContent = "Hello, User2!";

//        // Mocking User Repository for retrieving users by email
//        when(chatRepository.findByRoomusers_UserIdIn(List.of(user1.getId(), user2.getId()))).thenReturn(Optional.empty());

        // Create a new ChatRoom and associate users with it
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomName("채팅방");
        System.out.println("Created ChatRoom: " + chatRoom.getRoomName());
        // Create RoomInUsers and associate with the chat room
        RoomInUsers roomInUsers = new RoomInUsers();
        roomInUsers.setChatRoom(chatRoom);
        roomInUsers.setUser(user1);
        System.out.println("RoomInUsers for User1: " + roomInUsers.getUser().getName());

        RoomInUsers roomInUsers1 = new RoomInUsers();
        roomInUsers1.setChatRoom(chatRoom);
        roomInUsers1.setUser(user2);
        System.out.println("RoomInUsers for User2: " + roomInUsers1.getUser().getName());

//
//        roomInUserRepository.save(roomInUsers);  // Persist the RoomInUsers object for user1
//        roomInUserRepository.save(roomInUsers1);
        // Add RoomInUsers objects to the ChatRoom's roomusers collection
        chatRoom.getRoomusers().add(roomInUsers);
        chatRoom.getRoomusers().add(roomInUsers1);
        System.out.println("Added RoomInUsers to ChatRoom: " + chatRoom.getRoomusers().size());
        // Call the method that should save the chat room and users
        System.out.println("Calling createOneOnOneChatRoom method...");
//        chatService.createOneOnOneChatRoom(user1.getId(), user2.getId());

        System.out.println("Verifying save calls...");
        verify(chatRepository).save(any(ChatRoom.class));  // Verify that save was called for chat room
        verify(roomInUserRepository, times(2)).save(any(RoomInUsers.class));  // Verify RoomInUsers save was called twice
    }





}
