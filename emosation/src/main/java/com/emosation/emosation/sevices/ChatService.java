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
import com.google.gson.Gson;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {


    private final ChatRepository chatRepository;
    private final UserService userService;
    private final RoomInUserRepository roomInUserRepository;
    private final Object lock = new Object();
    private final EntityManager em;
    private final RedisChatService redisChatService;

    @Autowired
    public ChatService(ChatRepository chatRepository, UserService userService, RoomInUserRepository roomInUserRepository,EntityManager em,RedisChatService redisChatService ) {

        this.em = em;
        this.redisChatService = redisChatService;
        this.roomInUserRepository = roomInUserRepository;
        this.userService = userService;
        this.chatRepository = chatRepository;

    }

    @Transactional
    public Optional<RoomDTO> findChatroomByUser(String senderem,String receiverem){

        User sender = userService.getUser(senderem);
        User receiver = userService.getUser(receiverem);

        System.out.println("기존 채팅방 확인중" + receiverem);

        List<Long> userIds = Arrays.asList(sender.getId(),receiver.getId());
        Optional<ChatRoom> existingRoom = chatRepository.findByRoomusers_UserIdIn(userIds,2); // 여기서 상대가 채팅방 나가기를 하면 chatroom은 empty일것임.

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
                               roomInUser.getUser().getName(),roomInUser.getUser().getPics(),roomInUser.getUser().getRegisterd_at().toString(),roomInUser.getUser().getStatus()) ) // UserDTO로 변환 .. 여기서도 user객체 자체를 roodto에 넣으면 또 순환참조 문제가 생기는걸 확인 그래서 또 참조 문제 발생 필드 제외
                       .collect(Collectors.toList());

            return  Optional.of(new RoomDTO(chatRoom.getId(), chatRoom.getRoomName(), userDTOs, chatRoom.getOpenedAt()));
        }
        return Optional.empty();
    }





    // 여기서는 user 객체 하나가 연관된 모든 chatroom 객체를 roomdto객체로 변환후 반환 내 채팅방 조회시에 쓰일예정
    public List<RoomDTO> findMyRoom(String userEm){
        User user = userService.getUser(userEm);
        List<RoomInUsers> roomInUsers = roomInUserRepository.findByUser(user);
        List<RoomDTO> chatRooms = new ArrayList<>();



        for(RoomInUsers roomInUser : roomInUsers){

            ChatRoom chatRoom =  roomInUser.getChatRoom();
            String[] names = chatRoom.getRoomName().split("-");
            String userName1 = names[0]; // 첫 번째 사용자 이름
            String userName2 = names[1]; // 두 번째 사용자 이름
            String roomName = user.getName().equals(userName1)? userName2 : userName1;

            List<UserDTO> roomuser= new ArrayList<>();
            for(RoomInUsers roomusers : chatRoom.getRoomusers()){
                UserDTO userDTO = new UserDTO(
                        roomusers.getUser().getId(),
                        roomusers.getUser().getName(),
                        roomusers.getUser().getEmail(),
                        roomusers.getUser().getPics(),
                        roomInUser.getUser().getRegisterd_at().toString(),
                        roomInUser.getUser().getStatus());

                roomuser.add(userDTO);
            }

            RoomDTO roomDTO = new RoomDTO(
                    chatRoom.getId(),
                    roomName,
                    roomuser,
                    chatRoom.getOpenedAt()
            );

            chatRooms.add(roomDTO);
        }
        return chatRooms;
    }


    @Transactional
    public Optional<RoomDTO> getRoomById(Long roomId){
        if(roomId != null){
           Optional<ChatRoom> chatRoom = chatRepository.findById(roomId);

           if(chatRoom.isPresent()){
               ChatRoom chatRoom1 = chatRoom.get();
               List<UserDTO> userDTOS = chatRoom1.getRoomusers().stream().map(roomUser -> userService.convertToUserDto(roomUser.getUser().getEmail())).toList();

               RoomDTO roomDTO = new RoomDTO(chatRoom1.getId(),chatRoom1.getRoomName(),userDTOS,chatRoom1.getOpenedAt());

               return Optional.of(roomDTO);

           }

        }
        return Optional.empty();


    }



    @Transactional
    public RoomDTO createOneOnOneChatRoom(String sender,String receiver){

        User user = userService.getUser(sender);
        User reciver = userService.getUser(receiver);
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
                                    roomInuser.getUser().getPics(),roomInuser.getUser().getRegisterd_at().toString(),roomInuser.getUser().getStatus())).collect(Collectors.toList());
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
                                roomInuser.getUser().getPics(),roomInuser.getUser().getRegisterd_at().toString(),roomInuser.getUser().getStatus())).collect(Collectors.toList());
                RoomDTO roomDTO = new RoomDTO(chatRoom.getId(), chatRoom.getRoomName(), userDTOs, chatRoom.getOpenedAt());
                return roomDTO;
            }


            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setRoomName(user.getName() + "-" + reciver.getName());
            chatRepository.save(chatRoom);

            redisChatService.redisCreateChatRoom(chatRoom.getId(),user.getEmail(),reciver.getEmail());

            RoomInUsers roomInUsers = new RoomInUsers();
            roomInUsers.setChatRoom(chatRoom);
            roomInUsers.setUser(user);
            roomInUsers.setJoinedAt(LocalDateTime.now());
            chatRoom.getRoomusers().add(roomInUsers);
            roomInUserRepository.save(roomInUsers);

            RoomInUsers roomInUsers1 = new RoomInUsers();
            roomInUsers1.setChatRoom(chatRoom);
            roomInUsers1.setUser(reciver);
            roomInUsers1.setJoinedAt(LocalDateTime.now());
            chatRoom.getRoomusers().add(roomInUsers1);
            roomInUserRepository.save(roomInUsers1);


            redisChatService.redisCreateChatroomUsers(sender,receiver,chatRoom.getId());

            chatRepository.save(chatRoom);

            List<UserDTO> userDTOS = chatRoom.getRoomusers().stream()
                    .map(roomInUser-> new UserDTO(
                            roomInUser.getUser().getId(),
                            roomInUser.getUser().getEmail(),
                            roomInUser.getUser().getName(),
                            roomInUser.getUser().getPics(),
                            roomInUser.getUser().getRegisterd_at().toString(),
                            roomInUser.getUser().getStatus())).collect(Collectors.toList());

            RoomDTO roomDTO = new RoomDTO(chatRoom.getId(),chatRoom.getRoomName(),userDTOS,chatRoom.getOpenedAt());
            return roomDTO;

        }

    }




    public Map<String,Object> checkRoomInUsers(String senderem,String receiverem,Long roomId){ // roomId를 기준으로 chatroom 조회후 chatroom 에 연결된 RoomInUser 검증

        Map<String,Object> map = new HashMap<>();


        if(roomId != null){
            Optional<ChatRoom> chatRoom = chatRepository.cfindByRoomId(roomId);

            if(chatRoom.isPresent()){
                ChatRoom room = chatRoom.get();

                boolean iseSenderInRoom = room.getRoomusers().stream().anyMatch(roomInUser -> roomInUser.getUser().getEmail().equals(senderem));
                boolean iseReceiverInRoom = room.getRoomusers().stream().anyMatch(roomInUser -> roomInUser.getUser().getEmail().equals(receiverem));



                map.put("isSenderInRoom",iseSenderInRoom);
                map.put("isReceiverInRoom",iseReceiverInRoom);
                return map;

            }
        }
        map.put("NotExist",true);
        return map;

    }

    @Transactional
    public void addRoomInUser(String em,Long roomId){
        User user = userService.getUser(em);
        Optional<ChatRoom> room = chatRepository.findById(roomId);
        if(user!= null && room.isPresent()){
            ChatRoom chatRoom = room.get();
            RoomInUsers roomInUsers = new RoomInUsers();
            roomInUsers.setUser(user);
            roomInUsers.setChatRoom(chatRoom);
            roomInUsers.setJoinedAt(LocalDateTime.now());
            roomInUserRepository.save(roomInUsers);

            chatRoom.getRoomusers().add(roomInUsers);
            chatRepository.save(chatRoom);

        }


    }




    @Transactional
    public List<UserDTO> getRoomInUsersById(Long roomId){
        Optional<ChatRoom> chatRoom = chatRepository.findById(roomId);
        if(chatRoom.isPresent()){
            ChatRoom chatRoom1 = chatRoom.get();
            List<UserDTO> user = chatRoom1.getRoomusers().stream().map(
                    users -> new UserDTO(users.getUser().getId(),
                            users.getUser().getName(),
                            users.getUser().getEmail(),
                            users.getUser().getPics(),
                            users.getUser().getRegisterd_at().toString(),
                            users.getUser().getStatus())).toList();

            return user;
        }
        return Collections.emptyList();
    }






}
