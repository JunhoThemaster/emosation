package com.emosation.emosation.controller;


import com.emosation.emosation.model.friend.Friends;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.sevices.FriendService;
import com.emosation.emosation.sevices.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class HomeController {

    private final UserService userService;
    private final FriendService friendService;

    @Autowired
    public HomeController(FriendService friendService,UserService userService) {
        this.userService = userService;
        this.friendService = friendService;
    }



    @GetMapping("/")
    public String home() {
        return "home/home";
    }

    @GetMapping("/main")
    public String main() {


        return "home/main";
    }
}
