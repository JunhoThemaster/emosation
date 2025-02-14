package com.emosation.emosation.controller;


import com.emosation.emosation.Util.JwtUtil;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.model.user.UserDTO;
import com.emosation.emosation.sevices.RedisMessageService;
import com.emosation.emosation.sevices.RedisSessionService;
import com.emosation.emosation.sevices.RedisUserService;
import com.emosation.emosation.sevices.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {




    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RedisMessageService redisMessageService;
    private final RedisSessionService redisSessionService;
    private final RedisUserService redisUserService;

    @Autowired
    public AdminController(UserService userService, JwtUtil jwtUtil, RedisMessageService redisMessageService, RedisSessionService redisSessionService,RedisUserService redisUserService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.redisMessageService = redisMessageService;
        this.redisSessionService = redisSessionService;
        this.redisUserService = redisUserService;
    }

    @GetMapping("/adm")
    public String home(){

        return "/Admin/AdLog";

    }

    @GetMapping("/admin/home")
    public String adminHome(){


        return "/Admin/AdHome";
    }




    @PostMapping("/admin/login")
    public ResponseEntity<Map<String,Object>> login(@RequestParam String em, @RequestParam String pw) {
        Map<String,Object> res = new HashMap<>();
        System.out.println(em);
        System.out.println(pw);

        boolean login = userService.login(em,pw);
        if(login){
            boolean isAdmin = userService.isAdmin(em);
            if(isAdmin){
                String accessToken = jwtUtil.generateToken(em);
                String refreshToken = jwtUtil.generateRefToken(em);


                res.put("accessToken", accessToken);
                res.put("refreshToken", refreshToken);
                res.put("msg","관리자 접속.");
                return ResponseEntity.ok(res);
            }

        }
        res.put("msg","관리자에게 문의하세요");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);

    }

    @GetMapping("/admin/loadUsers")
    public ResponseEntity<Map<String,Object>> loadUsers(String curUser,@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page,size);


        Page<UserDTO> userDTOS = userService.getUsers(pageable);
        Map<String,Object> res = new HashMap<>();

        if(userDTOS != null){

            res.put("userLi", userDTOS.getContent());
            res.put("totalPage",userDTOS.getTotalPages());
            return ResponseEntity.ok(res);
        }
        res.put("msg","유저 없음");


        return ResponseEntity.ok(res);

    }


    @GetMapping("/admin/sessionCnt")
    public ResponseEntity<Map<String,Object>> sessionCnt() {

        Integer sessCnt = redisSessionService.getCurSessionCnt();

        Map<String,Object> res = new HashMap<>();
        res.put("curSsCnt",sessCnt);


        return ResponseEntity.ok(res);


    }


    @GetMapping("/admin/dayschart")
    public ResponseEntity<Map<String,Object>> dayschart() {
        Map<String,Object> res = new HashMap<>();


        Map<String,Object> charts = redisSessionService.getLast7daysCnt();

        if(charts != null){

            return ResponseEntity.ok(charts);

        }

        res.put("msg","정보가 존재하지않습니다");

        return ResponseEntity.ok(res);


    }


    @PatchMapping("/admin/deleteUser")
    public ResponseEntity<Map<String,Object>> softDeleteUser(@RequestParam String userEm) {

        redisUserService.setBackUpPointforUser(userEm); // 백업포인트를 redis에 저장 aof방식과 rdb방식 둘다 쓰기로함 ... RDB스냅샷 주기안에 변경된 사항들은 저장이안되더라


        Map<String,Object> res = new HashMap<>();

        try{
            boolean isSuccess = userService.deleteUser(userEm);


            if(isSuccess){
                res.put("msg" , "유저 삭제 완료");
                return ResponseEntity.ok(res);
            }else{
                res.put("msg","유저 찾기 실패");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
            }
        } catch (DataAccessException e) {
            res.put("msg","트랜잭션 실패");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        } catch (Exception e) {
            res.put("msg","예상치 못한 오류");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }


    @PatchMapping("/admin/rollbackUser")
    public ResponseEntity<Map<String,Object>> rollbackUser(@RequestParam String userEm){

        boolean isRecovered = redisUserService.rollBackUser(userEm);
        System.out.println(userEm);
        Map<String,Object> res = new HashMap<>();

        if(isRecovered){
            res.put("msg","계정복구가 완료되었습니다");
            res.put("isRecovered",isRecovered);
            return ResponseEntity.ok(res);

        }
        res.put("msg","계정복구 기간 만료 혹은 복구 불가");
        res.put("isRecovered",isRecovered);
        return ResponseEntity.ok(res);

    }




}
