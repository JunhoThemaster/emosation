package com.emosation.emosation.websocket;

import com.emosation.emosation.model.chat.ChatRoom;
import com.emosation.emosation.model.chat.Message;
import com.emosation.emosation.model.chat.RoomDTO;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.model.user.UserDTO;
import com.emosation.emosation.repository.ChatRepository;
import com.emosation.emosation.sevices.*;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import jakarta.mail.Session;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyWsHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(MyWsHandler.class);

    private final WsSessionManager wsSessionManager;
    private final UserService userService;
    private final ChatService chatService;
    private final RedisChatService redisChatService;
    private final OpenAiService openAiService;
    private final RedisMessageService redisMessageService;
    private final RedisSessionService redisSessionService;
    // stomp를 사용하여  구독 uri를 통한 메세지 전송시에 session정보가 명시적으로 전달되지않아서 전송시에 대상이되는 session의 정보를 불러오지 못했음..
    // 그래서 세션의 정보를 명시적으로 지정하는 방법을 찾아보니 websocket api만을 이용한 방법이 있었음



    public MyWsHandler(WsSessionManager wsSessionManager,
                       ChatService chatService ,RedisChatService redisChatService,OpenAiService openAiService,RedisMessageService redisMessageService,
                       RedisSessionService redisSessionService,UserService userService) {
        this.redisMessageService = redisMessageService;
        this.userService = userService;
        this.redisChatService = redisChatService;
        this.chatService  = chatService;
        this.wsSessionManager = wsSessionManager;
        this.openAiService = openAiService;
        this.redisSessionService = redisSessionService;

    }  // 핸들러에서 필요한 의존성 주입 socket정의 클래스에서부터 주입해주어야함.



    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userEm = getUserEmailFromSession(session); // email로 session의 정보를 명시적으로 등록해주니 정상적으로 아주 빠르게 메세지 전달과 읽기가 가능해졌다.
                                                            // 세션이 등록되기전에 beforehandshake에서 jwt를 검증하는 과정에서 검증후 파싱하여 email추출이 가능함
        wsSessionManager.addSession(userEm,session);

        redisSessionService.incrementSession(userEm);
        redisSessionService.setDailyCnt(userEm);

        System.out.println("현재 접속자수 : " + redisSessionService.getCurSessionCnt());
        System.out.println("오늘의 접속자수 :" + redisSessionService.getDailyCnt());

        System.out.println("세션이 등록되었습니다. 이메일: " + userEm);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

        JsonObject jsonMessage = JsonParser.parseString(message.getPayload().toString()).getAsJsonObject();

        JsonObject payload = jsonMessage.has("payload") ? jsonMessage.getAsJsonObject("payload") : null;

        String type = jsonMessage.get("type").getAsString();

        if("message".equals(type) && payload != null) {

            handleRoomExceptionAndMessage(session,payload,jsonMessage);

        } else if ("enterRoom".equals(type)) {

            System.out.println("채티방 입장");
            handleTypeEnter(session,jsonMessage);

        } else if ("close".equals(type)) {
            // 상대방이 채팅방을 닫기 버튼을 눌렀을때 전송되는 메세지의타입 = close 로 설정함.


            handleTypeClose(session,jsonMessage);

        } else if("to-All".equals(type)) {

            adminSays(payload);

        }

    }


    public void handleRoomExceptionAndMessage(WebSocketSession session, JsonObject payload,JsonObject jsonMessage ) throws Exception {

        String sender = payload.has("sender") ? payload.get("sender").getAsString() : null;
        if (sender == null) {
            throw new IllegalArgumentException("Sender 정보가 없습니다.");
        }
        String receiver =  URLDecoder.decode(jsonMessage.get("destination").getAsString(), StandardCharsets.UTF_8);


        Long roomId = redisChatService.getRoomIdFromRedis(sender,receiver);

        Optional<RoomDTO> roomDTO = chatService.getRoomById(roomId);


        if(!roomDTO.isPresent()) {
            roomDTO = Optional.ofNullable(createChatroom(sender, receiver, session));

        }

        WebSocketSession recSession = wsSessionManager.getSession(receiver);

        boolean isSessionInRoom = wsSessionManager.isSessionInRoom(roomDTO.get().getId(),recSession);

        if(isSessionInRoom){
            handleMsgForTrgtIsReading(recSession, roomDTO.get().getId(),receiver , payload.get("sender").getAsString(),payload);
        }else {
            handleMsgForTrgtNotNull(recSession,roomDTO.get().getId(), receiver, payload.get("sender").getAsString(), payload);
        }
    }


    public RoomDTO createChatroom(String sender, String receiver,WebSocketSession session) throws Exception {
        RoomDTO roomDTO = chatService.createOneOnOneChatRoom(sender, receiver);
        WebSocketSession trgtSession = wsSessionManager.getSession(receiver);

        announceToClientForNewOne(session,sender,trgtSession,roomDTO);

        return roomDTO;

    }

    private void announceToClientForNewOne(WebSocketSession session, String sender, WebSocketSession trgtSession,RoomDTO roomDTO) throws Exception{
        JsonObject msgObj = new JsonObject();
        msgObj.addProperty("type", "newOne");
        msgObj.addProperty("sender", sender);
        msgObj.addProperty("roomId", roomDTO.getId());

        trgtSession.sendMessage(new TextMessage(msgObj.toString()));
        session.sendMessage(new TextMessage(msgObj.toString()));

    }



    public void handleMsgForTrgtNotNull(WebSocketSession targetSession,Long roomId,String recipientEmail1,String sender,JsonObject payload ) throws IOException{
        redisMessageService.setUnreadCnt(recipientEmail1,roomId,0);
        redisMessageService.incrementUnreadCnt(recipientEmail1 ,roomId);

        JsonObject msgObj = new JsonObject();

        msgObj.addProperty("type","message");
        msgObj.addProperty("roomId",roomId);
        msgObj.addProperty("sender",sender);
        msgObj.addProperty("recipient",recipientEmail1);
        msgObj.addProperty("content",payload.get("content").getAsString());
        msgObj.addProperty("time",LocalDateTime.now().toString());
        msgObj.addProperty("newMsg",redisMessageService.getUnreadCnt(recipientEmail1,roomId));
        redisMessageService.saveMsgtoRedis(sender,roomId, payload.get("content").getAsString(),LocalDateTime.now());


        if(targetSession.isOpen() && targetSession != null){
            targetSession.sendMessage(new TextMessage(msgObj.toString()));
        } else if(targetSession == null){
            System.out.println("상대 세션 없음");
        }

    }
    private void handleMsgForTrgtIsReading(WebSocketSession targetSession,Long roomId,String recipientEmail1,String sender,JsonObject payload) throws IOException{
        String sentimentResult = openAiService.genRepsAsyn(payload.get("content").getAsString()); // 기존 googleNLP를 이용하여 긍정부정 강도 점수로 sentiment 객체를 만들어주었는데 이젠 그럴필요없다.
        JsonObject msgObj = new JsonObject();
        msgObj.addProperty("type","message");
        msgObj.addProperty("roomId", roomId); // roomId를 전달하는 이유는 메세지 출력시에 각기 다른 채팅방의 메세지가 채팅방에 모두 출력되는 문제가 생김
        msgObj.addProperty("sender" , sender);
        msgObj.addProperty("content" ,  payload.get("content").getAsString());
        msgObj.addProperty("recipient" , recipientEmail1);
        msgObj.addProperty("time" , LocalDateTime.now().toString());
        msgObj.addProperty("sentiment" , sentimentResult); // 클라이언트가 받게될 jsonobj 에다가 gpt의 결과 문자열 넣고 클라이언트에서 파싱하면 끝.

        targetSession.sendMessage(new TextMessage(msgObj.toString()));
        redisMessageService.saveMsgtoRedis(sender,roomId, payload.get("content").getAsString(), LocalDateTime.now());

    }

    private void handleTypeEnter(WebSocketSession session,JsonObject payload) throws IOException {
        Long roomId = payload.get("roomId").getAsLong();
        String sender = payload.get("sender").getAsString();
        logger.debug("Users EnterRoom {}: {}", roomId, sender);
        Map<String,String> users = redisChatService.redisGetUserByChatroom(roomId);
        String receiver = null;

        if(users != null){
            String user1 = users.get("user1");
            String user2 = users.get("user2");
            if (user1.equals(sender)){
                receiver = user2;
            }else if(user2.equals(sender)){
                receiver = user1;
            }
        }else{
            logger.debug("There's no user for receive");
            System.out.println("there is no users for this");
        }

        WebSocketSession trgtSession = wsSessionManager.getSession(receiver);
        boolean isSessioninRoom = wsSessionManager.isSessionInRoom(roomId,trgtSession);
        wsSessionManager.addRoomSession(roomId,session);
        redisMessageService.resetUnreadCnt(sender,roomId); // 채팅방 입장시 리셋

        if(trgtSession != null && isSessioninRoom ){

            JsonObject response = new JsonObject();
            response.addProperty("type","status");
            response.addProperty("roomId",roomId);
            response.addProperty("doesSheInRoom", true);  // boolean 값을 클라이언트로 전달

            session.sendMessage(new TextMessage(response.toString()));
            trgtSession.sendMessage(new TextMessage(response.toString()));

        }else if(trgtSession == null){

            redisMessageService.incrementUnreadCnt(receiver,roomId);
            JsonObject response = new JsonObject();
            response.addProperty("type","status");
            response.addProperty("roomId",roomId);
            response.addProperty("doesSheInRoom", false);

            session.sendMessage(new TextMessage(response.toString()));
            System.out.println("접속한 상대 세션 없음");
        }

    }

    private void handleTypeClose(WebSocketSession session,JsonObject payload) throws IOException {
        Long roomId = payload.get("roomId").getAsLong();
        System.out.printf("closing room :",roomId);
        String sem = payload.get("semail").getAsString(); // 채팅방 닫기를 누른 사람.
        System.out.printf("closing user:", sem);
        logger.debug("User closed",roomId,sem);

        JsonObject response = new JsonObject();

        response.addProperty("type","status");

        response.addProperty("roomId",roomId);

        response.addProperty("doesSheInRoom", false);
        wsSessionManager.removeSessionFromRoom(roomId,sem);

        System.out.println("룸에서 현재 세션 삭제"  +sem);

        if(wsSessionManager.getRoomSession(roomId)){
            WebSocketSession trgtSession = wsSessionManager.getSessionFromRoom(roomId);
            if(trgtSession.isOpen() && trgtSession != null) {
                session.sendMessage(new TextMessage(response.toString()));
                trgtSession.sendMessage(new TextMessage(response.toString()));
            }else{
                System.out.println("세션 없음");
            }
        }

    }

    private void adminSays(JsonObject payload) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<UserDTO> users  = userService.getAllUsers();
        Map<String,WebSocketSession> sessions = wsSessionManager.getAllsessions();
        for(UserDTO user : users){
            String userEm = user.getEmail();
            String senderEm = payload.get("sender").getAsString();
            RoomDTO roomDTO = chatService.createOneOnOneChatRoom(senderEm,userEm);
            executor.submit(() -> {
                try {

                    WebSocketSession session = sessions.get(userEm);

                    if (session != null && session.isOpen()) {
                        Long roomId = roomDTO.getId();

                        JsonObject msgObj = new JsonObject();
                        msgObj.addProperty("type", "newOne");
                        msgObj.addProperty("roomId", roomId);

                        JsonObject msgObj2 = new JsonObject();
                        msgObj2.addProperty("type", "message");
                        msgObj2.addProperty("content", payload.get("content").getAsString());
                        msgObj2.addProperty("sender", payload.get("sender").getAsString());

                        session.sendMessage(new TextMessage(msgObj.toString()));
                        session.sendMessage(new TextMessage(msgObj2.toString()));
                    } else{
                        redisMessageService.saveMsgtoRedis(payload.get("sender").getAsString(), roomDTO.getId(), payload.get("content").getAsString(),LocalDateTime.now());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }



    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {


    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String userEmail = getUserEmailFromSession(session);
        wsSessionManager.removeSession(userEmail);

        redisSessionService.setSessionOff(userEmail);
        System.out.println("세션이 종료되었습니다. 이메일: " + userEmail);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


    @PreDestroy
    public void destroy() {
       Map<String ,WebSocketSession> sessions =  wsSessionManager.getAllsessions();

        sessions.forEach((userEmail,session) -> {
            if(userEmail == null && session == null){
                return;
            }


            String em = getUserEmailFromSession(session);


            redisSessionService.setSessionOff(em);

            System.out.println("종료 직전 작업 수행");
        });

    }



    private String getUserEmailFromSession(WebSocketSession session) {
        // 예시: 헤더에서 이메일을 추출하거나 토큰을 통해 이메일을 찾는 방식 구현
        String userEmail = (String) session.getAttributes().get("userEmail");
        return userEmail;
    }

}
