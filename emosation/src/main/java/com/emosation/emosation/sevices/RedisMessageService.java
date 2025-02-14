package com.emosation.emosation.sevices;


import com.emosation.emosation.model.chat.ChatRoom;
import com.emosation.emosation.model.chat.Message;
import com.emosation.emosation.model.chat.MessageDTO;
import com.emosation.emosation.model.chat.RoomInUsers;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.model.user.UserDTO;
import com.emosation.emosation.repository.UserRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RedisMessageService {



    private final RedisTemplate<String, Object> redisTemplate;

    private final MessageService messageService;

    private final UserService userService;
    private final RedisChatService redisChatService;
    private final ChatService chatService;
    private final UserRepository userRepository;


    public RedisMessageService(RedisTemplate<String,Object> redisTemplate , MessageService messageService, UserService userService, RedisChatService redisChatService, ChatService chatService, UserRepository userRepository){

        this.userService = userService;
        this.messageService = messageService;
        this.redisTemplate = redisTemplate;
        this.redisChatService = redisChatService;
        this.chatService = chatService;
        this.userRepository = userRepository;
    }



    public void setUnreadCnt(String receiver,Long roomId,int count){
        String key = "unreadCnt:" + receiver + ":" + roomId; // 레디스의 키 밸류 설정

        redisTemplate.opsForValue().set(key, String.valueOf(count));
    }


    public Integer getUnreadCnt(String receiver,Long roomId){

        String key = "unreadCnt:" + receiver + ":" + roomId;

        String unreadCntStr = (String) redisTemplate.opsForValue().get(key);

        // 값이 없으면 0을 반환
        return (unreadCntStr != null) ? Integer.parseInt(unreadCntStr) : 0;

    }


    public void incrementUnreadCnt(String receiver,Long roomId){
        Integer unreadCnt = getUnreadCnt(receiver,roomId);
        String key = "unreadCnt:" + receiver + ":" + roomId;
        redisTemplate.opsForValue().increment(key, 1);


        System.out.println(getUnreadCnt(receiver,roomId));
    }


    public void resetUnreadCnt(String receiver,Long roomId){

        Integer unreadCnt = getUnreadCnt(receiver,roomId);

        if(unreadCnt > 0){
            setUnreadCnt(receiver,roomId,0);
        }
    }



    public void saveMsgtoRedis(String sender, Long roomId, String content, LocalDateTime sentedAt){
        String key = "chatroom:" + roomId + ":message";

        Map<String,Object> msgMap = new HashMap<String, Object>();

        Long msgId = redisTemplate.opsForList().size(key); // 메시지 개수 가져오기

        // 첫 번째 메시지일 경우, size()가 0이라면 초기값 설정
        if (msgId == null) {
            msgId = 0L;
        }
        msgMap.put("msgId",msgId);
        msgMap.put("roomId",roomId);
        Map<String,Object> senderInfo = new HashMap<>();
        UserDTO userDTO = userService.convertToUserDto(sender);
        String senderJson = new Gson().toJson(userDTO);
        msgMap.put("sender",senderJson);
        senderInfo.put("userDTO",userDTO);

        msgMap.put("content",content);

        String sentedAtSerial = sentedAt.toString();
        msgMap.put("sentedAt",sentedAtSerial);

        String msgJson = new Gson().toJson(msgMap);
        System.out.println(msgJson);
        redisTemplate.opsForList().rightPush(key, msgJson);

        redisTemplate.opsForList().trim(key, 0, 999);


    }



    public List<MessageDTO> getMsgListfrRedis(Long roomId) {

        String key = "chatroom:" + roomId + ":message";

        List<Object> msgJsonLi = redisTemplate.opsForList().range(key, 0, -1);

        if (msgJsonLi.isEmpty()) { // 첫조회시에는 redis메모리에 메세지가 저장되어있지 않을테니 redis에 db메세지들 저장
            List<MessageDTO> dbMsgList = messageService.findByChatRoom(roomId);

            for (MessageDTO msgDTO : dbMsgList) {
                UserDTO userem = msgDTO.getSender();


                saveMsgtoRedis(userem.getEmail(), msgDTO.getRoomId(), msgDTO.getContent(), msgDTO.getSentedAt()); // 호출시에 조회했다는 뜻이기에 redis에 저장될떄부터 true로 저장

            }

            return dbMsgList;


        } else { // 이후 조회시에는 redis에 조회된 메세지들이 있을테니 map객체로 변환후 추출  ->  dto로 변환후  반환함.
            List<MessageDTO> msgList = new ArrayList<>();
            for (Object obj : msgJsonLi) {
                String msgJson = (String) obj;

                Map<String, Object> msgMap = new Gson().fromJson(msgJson, Map.class);


                Long msgId = ((Double) msgMap.get("msgId")).longValue();
                String content = msgMap.get("content").toString();
                String senderJson = msgMap.get("sender").toString();


                LocalDateTime sentedAt = LocalDateTime.parse(msgMap.get("sentedAt").toString());

                MessageDTO messageDTO = null;
                UserDTO userDTO = new Gson().fromJson(senderJson,UserDTO.class);
                Optional<User> user = userRepository.findById(userDTO.getId());
                if (user.isPresent() && user.get().getStatus().equals(User.UserStatus.DELETED)) {
                    // 삭제된 사용자 처리
                    UserDTO deletedUserDTO = userService.convertToUserDto(user.get().getEmail());
                    messageDTO = new MessageDTO(msgId, content, deletedUserDTO, sentedAt, roomId);
                } else {
                    // 일반 사용자 처리
                    messageDTO = new MessageDTO(msgId, content, userDTO, sentedAt, roomId);
                }



                msgList.add(messageDTO);

            }
            System.out.println("redis조회");
            return msgList;


        }

    }


//
//    @PreDestroy
//    public void saveMsgToDb(Long roomId){
//        String key = "chatroom:" + roomId + ":message";
//
//        List<Object> msgJsonLi = redisTemplate.opsForList().range(key, 0, -1);
//
//
//            return dbMsgList;
//
//
//
//    }









}
