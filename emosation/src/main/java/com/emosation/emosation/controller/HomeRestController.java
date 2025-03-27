package com.emosation.emosation.controller;


import com.emosation.emosation.model.chat.ChatRoom;
import com.emosation.emosation.model.chat.Message;
import com.emosation.emosation.model.chat.MessageDTO;
import com.emosation.emosation.model.chat.RoomDTO;
import com.emosation.emosation.model.friend.FriendDTO;
import com.emosation.emosation.model.friend.Friends;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.model.user.UserDTO;
import com.emosation.emosation.sevices.*;
import lombok.Getter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RestController
public class HomeRestController {

    private final FriendService friendService;
    private final UserService userService;
    private final ChatService chatService;
    private final MessageService messageService;
    private final RedisMessageService redisMessageService;
    private final RedisChatService redisChatService;

    @Autowired
    public HomeRestController(FriendService friendService, UserService userService, ChatService chatService, MessageService messageService, RedisMessageService redisMessageService, RedisChatService redisChatService) {
        this.userService = userService;
        this.friendService = friendService;
        this.chatService = chatService;
        this.messageService = messageService;
        this.redisMessageService = redisMessageService;
        this.redisChatService = redisChatService;
    }

    @GetMapping("/main/myfrlist")
    public ResponseEntity<List<FriendDTO>> getFriends(@RequestParam String userid) {

        User user = userService.getUser(userid);


        List<FriendDTO> frList = friendService.findMyFriends(user);



        return ResponseEntity.ok(frList);
    }


    @GetMapping("/main/findfr")
    public ResponseEntity<Map<String,Object>> findFriends(@RequestParam String name) {


        Optional<UserDTO> user = userService.findUserByName(name);
        Map<String,Object> res = new HashMap<>();

        user.ifPresentOrElse(
                u -> res.put("user", u),   // 사용자 존재 시
                () -> res.put("msg", "찾은 사용자 없음")  // 사용자 없으면 메시지
        );


        System.out.println(user);

        return ResponseEntity.ok(res);
    }



    @PostMapping("/main/addfr/{userid}/{frid}")
    public ResponseEntity<Map<String,Object>> addFriend(@PathVariable Long frid, @PathVariable String userid) {
        Map<String,Object> res = new HashMap<>();


       try{
           User user = userService.getUser(userid);
           friendService.addFriend(user.getId(), frid);
            res.put("msg","추가 성공");
           return ResponseEntity.ok(res);
       }catch (Exception e){
            e.printStackTrace();

            res.put("errors",e.getMessage());
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
       }

    }


    @GetMapping("/main/chatlist")
    public ResponseEntity<Map<String,Object>> myRooms(@RequestParam String userEm) {
        Map<String,Object> res = new HashMap<>();

        try {
            List<RoomDTO> myrooms = chatService.findMyRoom(userEm);
            List<Map<String, Object>> roomLiWUnreadCnt = new ArrayList<>();
            if(myrooms != null && !myrooms.isEmpty()) {
                for(RoomDTO room : myrooms) {
                    Long roomId = room.getId();

                    Integer unreadCnt = redisMessageService.getUnreadCnt(userEm, roomId);
                    unreadCnt = (unreadCnt != null) ? unreadCnt : 0; // null 체크

                    Map<String, Object> roomWUnreadCnt = new HashMap<>();
                    roomWUnreadCnt.put("room", room);
                    roomWUnreadCnt.put("unread", unreadCnt);

                    roomLiWUnreadCnt.add(roomWUnreadCnt);


                }
                res.put("rooms",roomLiWUnreadCnt);
                return ResponseEntity.ok(res);

            }

            res.put("msg","you dont have any room");
            return ResponseEntity.ok(res);
        } catch (Exception e){
            res.put("errors",e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }


    }


    @GetMapping("/main/check")
    public ResponseEntity<Map<String,Object>> enterRoom(@RequestParam Long roomId) { // 기존 채팅방 입장시 redis에 저장된 메세지가 있다면 redis조회 없으면 기존 db조회후 redis에 저장.

        Map<String,Object> res = new HashMap<>();

        List<MessageDTO> rdmsgList = redisMessageService.getMsgListfrRedis(roomId);

        Optional<RoomDTO> roomDTO = chatService.getRoomById(roomId);

        List<UserDTO> userDTOS = new ArrayList<>();

        if(roomDTO.isPresent()) {                               // 기존에 msg리스트에 저장했던 UserDTO를 뽑아 보내는건 잘못됨. 왜냐면 상대가 메세지를 한번도 안보냈다면??? 그럼 없는것임.
           userDTOS =  roomDTO.get().getRoomUser();
        }


        if(rdmsgList == null) {
          res.put("msg","no rooms for read");
          return ResponseEntity.ok(res);
        }
        res.put("users",userDTOS);
        res.put("messages",rdmsgList);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/main/close/{roomId}/{em}")
    public ResponseEntity<Map<String,Object>> saveMsgToDb(@PathVariable Long roomId) {

        List<MessageDTO> rmsgList = redisMessageService.getMsgListfrRedis(roomId);
        Map<String,Object> res = new HashMap<>();

        try{
            if(rmsgList != null) {
                for(MessageDTO rmsg : rmsgList){
                    Optional<Message> msgLi = messageService.findByRoomAndRdmsId(roomId,rmsg.getId());

                    if(!msgLi.isPresent()) {    /// msgLi에서 rmsgId 로 존재여부를 검증했기때문에 존재하지 않는다면 각각 저장시키면됨.
                        UserDTO userDTO = rmsg.getSender();

                        messageService.save(rmsg.getId(),rmsg.getRoomId(),userDTO.getEmail(),rmsg.getContent());
                        System.out.println("메세지 최종 저장완료");
                    }

                }
                res.put("message","save  완료");
                return ResponseEntity.ok(res);
            } else{
                res.put("msg","save할  메세지가 없음");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(res);
            }
        } catch (Exception e){
            res.put("msg", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }


    }



    @GetMapping("/main/checkrooms/{frEm}/{userEm}")
    public ResponseEntity<Map<String,Object>> checkRoomsByUsers(@PathVariable String frEm, @PathVariable String userEm) { // 유저가 친구목록에서 메세지보내기 클릭시 연결된 chatroom검증

        Optional<RoomDTO> roomDTO = chatService.findChatroomByUser(userEm,frEm);
        System.out.println("채팅방" + roomDTO);
        Map<String,Object> res = new HashMap<>();
        if(roomDTO.isEmpty()) {
            res.put("msg","no rooms found create new one");
        }
        else{
            Long roomId = roomDTO.get().getId();
            List<MessageDTO> msgList = redisMessageService.getMsgListfrRedis(roomId);
            res.put("messages",msgList);
        }


        return ResponseEntity.ok(res);
    }
}
