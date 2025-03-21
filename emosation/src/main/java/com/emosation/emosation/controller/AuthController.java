package com.emosation.emosation.controller;


import com.emosation.emosation.Util.JwtUtil;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.sevices.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.xml.crypto.Data;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {


    private final UserService userService;
    private final JwtUtil jwtUtil;


    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }


    @GetMapping("/auth/resetPw")
    public String resetPw() {


        return "/auth/resetPw";
    }


    @GetMapping("/auth/mypage")
    public String mypage(){


        return "mypage/mypage";
    }




    @PostMapping("/auth/api/mypage")
    public ResponseEntity<Map<String, Object>> mypage(@RequestHeader("Authorization") String auth) {

        String token = auth.substring("Bearer ".length());

        String userEm = jwtUtil.extId(token);
        System.out.printf("마이페이지 조회",userEm);

        Map<String, Object> res = new HashMap<>();
        try{
            User user = userService.getUser(userEm);

            if (user != null) {
                String userEmail = user.getEmail();
                String userName = user.getName();
                BigInteger userPhone = user.getPhone();
                LocalDateTime date = user.getRegisterd_at();



                res.put("userEm", userEmail);
                res.put("userName", userName);
                res.put("userPhone", userPhone);
                res.put("date", date);
                return ResponseEntity.ok(res);
            }
            res.put("msg","user not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);


        }catch (DataAccessException e){
            res.put("msg", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }catch (NullPointerException e){
            res.put("msg", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);

        }


    }






}
