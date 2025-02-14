package com.emosation.emosation.redis;


import com.emosation.emosation.sevices.RedisSessionService;
import com.emosation.emosation.websocket.WsSessionManager;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@Component
public class RedisCycle implements SmartLifecycle {

    private final RedisSessionService redisSessionService;
    private final WsSessionManager wsSessionManager;
    private boolean running = false;

    public RedisCycle(RedisSessionService redisSessionService,WsSessionManager wsSessionManager) {
        this.wsSessionManager = wsSessionManager;
        this.redisSessionService = redisSessionService;
    }


    @Override
    public void start() {
        running = true;
        System.out.println("redis시작");
    }

    @Override
    public void stop() {
        try {
            Map<String, WebSocketSession> sessions =  wsSessionManager.getAllsessions();
            if(!sessions.isEmpty()){

                sessions.forEach((userEmail,session) ->{
                    if(userEmail !=null && session != null){
                    redisSessionService.setSessionOff(userEmail);
                        System.out.println("세션 처리 완료");
                    }
                });
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
        }


        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
    @Override
    public boolean isAutoStartup(){
        return true;
    }


    @Override
    public int getPhase(){
        return Integer.MAX_VALUE;
    }



}
