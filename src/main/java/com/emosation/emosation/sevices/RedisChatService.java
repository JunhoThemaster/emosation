package com.emosation.emosation.sevices;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RedisChatService {


    private final RedisTemplate<String, Object> redisTemplate;


    public RedisChatService(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;

    }

    public void redisCreateChatRoom(Long roomId ,String user1,String user2){
        Map<String,String> info = new HashMap<>();

        info.put("user1",user1);
        info.put("user2",user2);

        String key = "ChatRoom:" + roomId + ":";

        boolean  exists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
        if(exists){
            return;
        }


        redisTemplate.opsForHash().putAll(key,info);


    }
    public void redisCreateChatroomUsers(String user1,String user2,Long roomId){

        String[] users = {user1, user2};
        Arrays.sort(users); //  항상 동일한 기준을 가지고 정렬해서 키 생성을 해주면 조회시에도 정렬후 조회시 user1 과 user2의 순서가 바뀌게 되어도 조회가 가능하다.
        String key = "Chatroom:"+ users[0] + ":" + users[1] + ":";

        redisTemplate.opsForValue().set(key,roomId.toString());

    }

    public Long getRoomIdFromRedis(String user1, String user2) {
        List<String> users = Arrays.asList(user1, user2);
        Collections.sort(users);  // 사용자들을 정렬하여 동일한 순서로 키 생성

        String key = "Chatroom:" + users.get(0) + ":" + users.get(1) + ":";

        // Redis에서 값 읽어오기
        String roomIdStr = (String) redisTemplate.opsForValue().get(key);
        return roomIdStr != null ? Long.valueOf(roomIdStr) : null;
    }







    public Map<String,String> redisGetUserByChatroom(Long roomId){
        String key = "ChatRoom:" + roomId + ":";


        Map<Object, Object> allUsersObj = redisTemplate.opsForHash().entries(key);

        Map<String,String> allUsers = new HashMap<>();


        for(Map.Entry<Object,Object> entry : allUsersObj.entrySet()){
            allUsers.put((String) entry.getKey(),(String) entry.getValue());
        }


        return allUsers;

    }



}
