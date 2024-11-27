package com.emosation.emosation.websocket;

import groovy.transform.AutoImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class WsSessionManager {




    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    private final Map<Long, HashSet<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public void addSession(String userEmail, WebSocketSession session) {

        userSessions.put(userEmail,session);
    }


    public WebSocketSession getSession(String email) {

        return userSessions.get(email);

    }


    public void removeSession(String userEmail) {
        userSessions.remove(userEmail);  // 세션 종료 시 세션 제거
    }



    public void addRoomSession(Long roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, k -> new HashSet<>()).add(session);

    }
    public boolean getRoomSession(Long roomId) {
        HashSet<WebSocketSession> rsession = roomSessions.get(roomId);

        if(rsession == null || rsession.isEmpty()) {
            return false;

        } else{
            return true;
        }

    }

    public boolean isSessionInRoom(Long roomId,WebSocketSession trgtSession) {
        Set<WebSocketSession> session =roomSessions.get(roomId);
        if(session != null){

            return session.contains(trgtSession);
        }
        return false;
    }


    public void removeSessionFromRoom(Long roomId,String em) {
        Set<WebSocketSession> rsession =roomSessions.get(roomId);

        if(rsession != null){
          WebSocketSession session = getSession(em);
          boolean removed = rsession.remove(session);
        } else{
            return;
        }



    }

    public WebSocketSession getSessionFromRoom(Long roomId) {
        Set<WebSocketSession> rsession =roomSessions.get(roomId);

        if(rsession != null){
            return rsession.iterator().next();

        }else{
            return null;
        }


    }








}
