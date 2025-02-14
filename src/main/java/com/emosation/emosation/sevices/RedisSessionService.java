package com.emosation.emosation.sevices;


import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RedisSessionService {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String KEY = "sessionCnt:";
    private static final String DAILYCNT = "daily:Cnnection:Cnt:" + LocalDate.now() + ":";

    public void incrementSession(String userEm){
       String key = "session:user:" + userEm;


        if (Boolean.FALSE.equals(redisTemplate.hasKey(KEY))) {
            redisTemplate.opsForValue().set(KEY, "0"); // 초기값 0으로 설정
        }


       if(Boolean.FALSE.equals(redisTemplate.hasKey(key))){
           redisTemplate.opsForValue().set(key,"online");
       }


       redisTemplate.opsForValue().increment(KEY);

    }

    public void setSessionOff(String userEm){
        String key = "session:user:" + userEm;

        if(Boolean.TRUE.equals(redisTemplate.hasKey(key))){
            redisTemplate.opsForValue().set(key,"offline"); // 다시 offline으로 set해주면 online offline로그기록이 남을거임.
            redisTemplate.opsForValue().decrement(KEY);
        }

    }

    public Integer getCurSessionCnt(){

        String countStr = (String) redisTemplate.opsForValue().get(KEY);
        return (countStr != null) ? Integer.parseInt(countStr) : 0;
    }




    public void setDailyCnt(String user){

        String key = DAILYCNT + user;

        Boolean isSet = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(isSet)) {
            redisTemplate.opsForValue().set(key,"Logged");
            return;
        }

        if(Boolean.FALSE.equals(redisTemplate.hasKey(DAILYCNT))){
            redisTemplate.opsForValue().set(DAILYCNT,"0");
        }


        redisTemplate.opsForValue().increment(DAILYCNT); // 유저와 날짜별로 키가 존재하지않는다면 키값증가... 동일유저의 카운팅은 의미가없기에
        redisTemplate.opsForValue().set(key, "Logged");

    }

    public Integer getDailyCnt(){

        String key = DAILYCNT;
        String dCount = (String) redisTemplate.opsForValue().get(key);

        return (dCount != null) ? Integer.parseInt(dCount) : 0;
    }



    public Map<String,Object> getLast7daysCnt(){
        Map<String,Object> result = new LinkedHashMap<>();
        LocalDate curdate = LocalDate.now();

        for(int i = 0; i < 7; i++){

            LocalDate trgtDate = curdate.minusDays(i);

            String key = "daily:Cnnection:Cnt:" + trgtDate + ":";

            String count  = (String) redisTemplate.opsForValue().get(key);

            result.put(trgtDate.toString(), (count != null) ? Integer.parseInt(count) : 0);

        }
        return result;
    }
}
