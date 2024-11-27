package com.emosation.emosation.controller;


import com.emosation.emosation.model.chat.ChatRoom;
import com.emosation.emosation.model.chat.Message;
import com.emosation.emosation.model.chat.MessageDTO;
import com.emosation.emosation.model.chat.RoomDTO;
import com.emosation.emosation.model.friend.FriendDTO;
import com.emosation.emosation.model.friend.Friends;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.model.user.UserDTO;
import com.emosation.emosation.sevices.ChatService;
import com.emosation.emosation.sevices.FriendService;
import com.emosation.emosation.sevices.MessageService;
import com.emosation.emosation.sevices.UserService;
import lombok.Getter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
public class HomeRestController {

    private final FriendService friendService;
    private final UserService userService;
    private final ChatService chatService;
    private final MessageService messageService;

    @Autowired
    public HomeRestController(FriendService friendService,UserService userService,ChatService chatService,MessageService messageService) {
        this.userService = userService;
        this.friendService = friendService;
        this.chatService = chatService;
        this.messageService = messageService;
    }

    @GetMapping("/main/myfrlist")
    public ResponseEntity<List<FriendDTO>> getFriends(@RequestParam String userid) {

        User user = userService.getUser(userid);


        List<FriendDTO> frList = friendService.findMyFriends(user);



        return ResponseEntity.ok(frList);
    }


    @GetMapping("/main/findfr")
    public ResponseEntity<Map<String,Object>> findFriends(@RequestParam String name) {

        System.out.println("찾을 사용자 이름 : " + name );
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
        Map<Long, Long> unreadCounts = new HashMap<>();
        try {
            List<RoomDTO> myrooms = chatService.findMyRoom(userEm);

            if(myrooms != null && !myrooms.isEmpty()) {
                for(RoomDTO room : myrooms){
                    Long roomId = room.getId();
                    long unreadCnt = chatService.getUnreadMsg(roomId,userEm);
                    unreadCounts.put(roomId, unreadCnt);
                   }

                res.put("unreadCnt",unreadCounts);
                res.put("rooms",myrooms);
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
    public ResponseEntity<Map<String,Object>> enterRoom(@RequestParam Long roomId) {

        Map<String,Object> res = new HashMap<>();
        List<MessageDTO> msgList = messageService.findByChatRoom(roomId);

        List<UserDTO> userDTOS = chatService.findChatRoomUsers(roomId);
        if(msgList == null) {
          res.put("msg","no rooms for read");
        }

        res.put("users",userDTOS);
        res.put("messages",msgList);

        return ResponseEntity.ok(res);


    }

    @PatchMapping("/main/updateRead/{roomId}/{em}")
    public ResponseEntity<String> updateRead(@PathVariable Long roomId, @PathVariable String em) {

        boolean isSetted = messageService.updateReadStat(roomId,em);

        if(isSetted){

          return ResponseEntity.ok("모든 메세지 읽음 완료 ");
        } else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("트랜잭션 실패?");
        }

    }



    @GetMapping("/main/checkrooms/{frEm}/{userEm}")
    public ResponseEntity<Map<String,Object>> checkRoomsByUsers(@PathVariable String frEm, @PathVariable String userEm) {

        Optional<RoomDTO> roomDTO = chatService.findChatroomByUser(userEm,frEm);
        System.out.println("채팅방" + roomDTO);
        Map<String,Object> res = new HashMap<>();
        if(roomDTO.isEmpty()) {
            res.put("msg","no rooms found create new one");
        }
        else{
            Long roomId = roomDTO.get().getId();
            List<MessageDTO> msgList = messageService.findByChatRoom(roomId);
            res.put("messages",msgList);
        }


        return ResponseEntity.ok(res);
    }
}
