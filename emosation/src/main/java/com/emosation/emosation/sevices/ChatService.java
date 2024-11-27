package com.emosation.emosation.sevices;


import com.emosation.emosation.model.chat.ChatRoom;
import com.emosation.emosation.model.chat.Message;
import com.emosation.emosation.model.chat.RoomDTO;
import com.emosation.emosation.model.chat.RoomInUsers;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.model.user.UserDTO;
import com.emosation.emosation.repository.ChatRepository;
import com.emosation.emosation.repository.MessageRepository;
import com.emosation.emosation.repository.RoomInUserRepository;
import com.emosation.emosation.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {


    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final RoomInUserRepository roomInUserRepository;
    private final MessageRepository messageRepository;
    private final Object lock = new Object();
    @Autowired
    public ChatService(ChatRepository chatRepository, UserRepository userRepository, RoomInUserRepository roomInUserRepository, MessageRepository messageRepository) {

        this.roomInUserRepository = roomInUserRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Optional<RoomDTO> findChatroomByUser(String senderem,String receiverem){

        User sender = userRepository.findByEmail(senderem);
        User receiver = userRepository.findByEmail(receiverem);

        System.out.println("기존 채팅방 확인중" + receiverem);

        List<Long> userIds = Arrays.asList(sender.getId(),receiver.getId());
        Optional<ChatRoom> existingRoom = chatRepository.findByRoomusers_UserIdIn(userIds,2);

        //  이부분에서 어떤 쿼리문이 날라가는지 확인해보니 좀 이상함
                                                                                                        // 실무에서는 jpa만 단일로 사용하지는 않을거같음.. 어떤 쿼리문이 날라가는지도 모르는데 말이야..
                                                                                                        // 그래서  리포지토리 단에서 jpql 직접 명시해서 작성해주니 조회가능
                                                                                                        // 추후 querydsl을 통해 다시 만들어보자

      ///  두 유저객체 간 연결된 채팅방이 존재한다면. (물론 유저객채 둘다 있어야함 )
        if(existingRoom.isPresent()){

               ChatRoom chatRoom = existingRoom.get();

               List<UserDTO> userDTOs = chatRoom.getRoomusers().stream()
                       .map(roomInUser -> new UserDTO(roomInUser.getUser().getId(),
                               roomInUser.getUser().getEmail(),
                               roomInUser.getUser().getName(),roomInUser.getUser().getPics()) ) // UserDTO로 변환 .. 여기서도 user객체 자체를 roodto에 넣으면 또 순환참조 문제가 생기는걸 확인 그래서 또 참조 문제 발생 필드 제외
                       .collect(Collectors.toList());

            return  Optional.of(new RoomDTO(chatRoom.getId(), chatRoom.getRoomName(), userDTOs, chatRoom.getOpenedAt()));
        }

           return Optional.empty();
    }


    // 여기서는 user 객체 하나가 연관된 모든 chatroom 객체를 roomdto객체로 변환후 반환 내 채팅방 조회시에 쓰일예정
    public List<RoomDTO> findMyRoom(String userEm){
        User user = userRepository.findByEmail(userEm);
        List<RoomInUsers> roomInUsers = roomInUserRepository.findByUser(user);
        List<RoomDTO> chatRooms = new ArrayList<>();

        for(RoomInUsers roomInUser : roomInUsers){

            ChatRoom chatRoom =  roomInUser.getChatRoom();


            List<UserDTO> roomuser= new ArrayList<>();
            for(RoomInUsers roomusers : chatRoom.getRoomusers()){
                UserDTO userDTO = new UserDTO(
                        roomusers.getUser().getId(),
                        roomusers.getUser().getName(),
                        roomusers.getUser().getEmail(),
                        roomusers.getUser().getPics());

                roomuser.add(userDTO);
            }

            RoomDTO roomDTO = new RoomDTO(
                    chatRoom.getId(),
                    chatRoom.getRoomName(),
                    roomuser,
                    chatRoom.getOpenedAt()
            );

            chatRooms.add(roomDTO);
        }
        return chatRooms;
    }

    public long getUnreadMsg(Long roomId,String em){
        ChatRoom chatRoom = chatRepository.findById(roomId).orElseThrow(() ->  new RuntimeException("room not found"));
        User user = userRepository.findByEmail(em);
        List<Message> msgList = messageRepository.findByChatRoom(chatRoom);

        long unreadCount  = msgList.stream().filter(message -> !message.getSender().equals(user)).filter(message -> !message.getRead()).count();



        return unreadCount;
    }



    public List<UserDTO> findChatRoomUsers(Long roomId){

        Optional<ChatRoom> chatRoom = chatRepository.findById(roomId);
        List<UserDTO> userDTOS = new ArrayList<>();
        if(chatRoom.isPresent()){
            ChatRoom chatRoom1 = chatRoom.get();

            for(RoomInUsers roomInUser : chatRoom1.getRoomusers()){
                User user = roomInUser.getUser();
                UserDTO userDTO = new UserDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPics()

                );

                userDTOS.add(userDTO);
            }
        }



        return userDTOS;
    }






    @Transactional
    public RoomDTO createOneOnOneChatRoom(String sender,String receiver){

        User user = userRepository.findByEmail(sender);
        User reciver = userRepository.findByEmail(receiver);
        System.out.println(reciver.getName());

        if (user == null || reciver == null) {
            throw new RuntimeException("Invalid users");
        };

        List<Long> userIds = Arrays.asList(user.getId(),reciver.getId());
        Optional<ChatRoom> existingRoom = chatRepository.findByRoomusers_UserIdIn(userIds,2);

        if(existingRoom.isPresent()){
                    ChatRoom chatRoom = existingRoom.get();
                    List<UserDTO> userDTOs = chatRoom.getRoomusers().stream().map(
                            roomInuser -> new UserDTO(roomInuser.getUser().getId(),
                                    roomInuser.getUser().getEmail(),
                                    roomInuser.getUser().getName(),
                                    roomInuser.getUser().getPics())).collect(Collectors.toList());
                    RoomDTO roomDTO = new RoomDTO(chatRoom.getId(), chatRoom.getRoomName(), userDTOs, chatRoom.getOpenedAt());
                    return roomDTO;
            // 동시성 문제가 발생하였음... 만약 두 유저가 동시에 채팅을 보냈는데 두유저간 연결된 채팅방이 없다면 만드는 로직을 구현하던중.
            // 따라서 기존 채팅방 검증후 또다시 synchronized를 이용해 한번에 한번의 스레드 실행,
        }



        synchronized (lock){
            existingRoom = chatRepository.findByRoomusers_UserIdIn(userIds, 2);

            if(existingRoom.isPresent()){
                ChatRoom chatRoom = existingRoom.get();
                List<UserDTO> userDTOs = chatRoom.getRoomusers().stream().map(
                        roomInuser -> new UserDTO(roomInuser.getUser().getId(),
                                roomInuser.getUser().getEmail(),
                                roomInuser.getUser().getName(),
                                roomInuser.getUser().getPics())).collect(Collectors.toList());
                RoomDTO roomDTO = new RoomDTO(chatRoom.getId(), chatRoom.getRoomName(), userDTOs, chatRoom.getOpenedAt());
                return roomDTO;
            }


            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setRoomName(user.getName() + "-" + reciver.getName());
            chatRepository.save(chatRoom);

            RoomInUsers roomInUsers = new RoomInUsers();
            roomInUsers.setChatRoom(chatRoom);
            roomInUsers.setUser(user);
            roomInUsers.setJoinedAt(LocalDateTime.now());
            roomInUserRepository.save(roomInUsers);


            RoomInUsers roomInUsers1 = new RoomInUsers();
            roomInUsers1.setChatRoom(chatRoom);
            roomInUsers1.setUser(reciver);
            roomInUsers1.setJoinedAt(LocalDateTime.now());
            roomInUserRepository.save(roomInUsers1);

            chatRepository.save(chatRoom);

            List<UserDTO> userDTOS = chatRoom.getRoomusers().stream()
                    .map(roomInUser-> new UserDTO(roomInUser.getUser().getId(),roomInUser.getUser().getEmail(),roomInUser.getUser().getName(),roomInUser.getUser().getPics())).collect(Collectors.toList());

            RoomDTO roomDTO = new RoomDTO(chatRoom.getId(),chatRoom.getRoomName(),userDTOS,chatRoom.getOpenedAt());
            return roomDTO;

        }

    }




}
