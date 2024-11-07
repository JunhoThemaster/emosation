package com.emosation.emosation.controller;


import com.emosation.emosation.Util.JwtUtil;
import com.emosation.emosation.sevices.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }




    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody String info) {

        try{
            //
            JSONObject json = new JSONObject(info);
            String email = json.getString("Email");
            String name = json.getString("Name");
            BigInteger phone = json.getBigInteger("Phone");
            String pw = json.getString("Pw");

            userService.save(email,name,pw,phone);

            return ResponseEntity.ok().build();
        } catch (Exception e){
            System.out.println("가입 실패" + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

        @PostMapping("/login")
        public ResponseEntity<Map<String, Object>> login(@RequestParam String email, @RequestParam String pw) {
            Map<String,Object> res = new HashMap<>(); // 응답 메세지를 보낼 키쌍

            boolean isValid = userService.login(email, pw);

            if(isValid){

                String accessToken = jwtUtil.generateToken(email);
                String refreshToken = jwtUtil.generateRefToken(email);
                res.put("accessToken",accessToken);
                res.put("refreshToken",refreshToken);
                res.put("msg","로그인 성공");
                return ResponseEntity.ok(res);
            } else{
                res.put("msg","이메일 또는 비밀번호가 정확하지않습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }



        }


    @PostMapping("/chekLogin")
    public ResponseEntity<Map<String, Object>> checkLogin(@RequestHeader("Authorization") String auth,
                                                          @RequestHeader(value = "Refresh-Token") String refreshToken) {


        if(auth == null || !auth.startsWith("Bearer ")){
            System.err.println("헤더 정보가 없습니다: Authorization 헤더가 잘못되었습니다.");
            return ResponseEntity.status(401).body((Map.of("message", "헤더정보가 없습니다")));
        }

        String token = auth.substring(7);
        try {

            if(jwtUtil.isExp(token)){
                System.err.println("액세스 토큰 만료됨: 토큰이 만료되었습니다.");
                if(refreshToken == null || jwtUtil.isExp(refreshToken)){
                    System.err.println("리프레시 토큰 만료됨: 리프레시 토큰이 없거나 만료되었습니다.");
                    return ResponseEntity.status(401).body(Map.of("message", "리프레시 토큰이 만료되었거나 존재하지 않습니다"));
                }
                String newAccessToken = jwtUtil.generateToken(jwtUtil.extId(refreshToken));
                System.out.println("새로운 액세스 토큰 발급됨: " + newAccessToken);
                String userId = jwtUtil.extId(newAccessToken);
                Map<String, Object> response = new HashMap<>();
                response.put("userId",userId);
                response.put("accessToken", newAccessToken);
                return ResponseEntity.ok(response);
            }
            String userId = jwtUtil.extId(token);
            Map<String, Object> response = new HashMap<>();
            response.put("userId",userId);
            response.put("accessToken", token);
            return ResponseEntity.ok(response);

        } catch (ExpiredJwtException e) {
            System.err.println("만료된 JWT 토큰: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("message", "토큰 만료"));
        } catch (SignatureException e) {
            // 서명 오류 처리
            return ResponseEntity.status(401).body(Map.of("message", "토큰 서명 오류"));
        } catch (IllegalArgumentException e) {
            // 토큰 없음
            return ResponseEntity.status(400).body(Map.of("message", "토큰이 존재하지 않습니다"));
        } catch (Exception e) {
            // 일반적인 예외 처리
            System.err.println("서버 오류: " + e.getMessage()); // 로그에만 출력
            return ResponseEntity.status(500).body(Map.of("message", "서버 오류 발생"));
        }
    }





}
