package com.emosation.emosation.controller;


import com.emosation.emosation.model.friend.FriendDTO;
import com.emosation.emosation.model.friend.Friends;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.model.user.UserDTO;
import com.emosation.emosation.sevices.FriendService;
import com.emosation.emosation.sevices.UserService;
import lombok.Getter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Autowired
    public HomeRestController(FriendService friendService,UserService userService) {
        this.userService = userService;
        this.friendService = friendService;
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
        if (user.isPresent()) {
            res.put("user", user.get());  // 값이 있을 경우 "user"로 저장
        } else {
            res.put("msg", "찾은 사용자 없음");  // 값이 없을 경우 메시지 저장
        }


        return ResponseEntity.ok(res);
    }
}
