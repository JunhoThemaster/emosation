package com.emosation.emosation.websocket;

import com.emosation.emosation.model.chat.ChatRoom;
import com.emosation.emosation.model.chat.Message;
import com.emosation.emosation.model.chat.RoomDTO;
import com.emosation.emosation.repository.ChatRepository;
import com.emosation.emosation.sevices.ChatService;
import com.emosation.emosation.sevices.GoogleNLPService;
import com.emosation.emosation.sevices.MessageService;
import com.emosation.emosation.sevices.OpenAiService;
import com.google.cloud.language.v1.Sentiment;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import org.json.JSONObject;
import org.springframework.web.socket.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Optional;

public class MyWsHandler implements WebSocketHandler {


    private final WsSessionManager wsSessionManager;
    private final ChatService chatService;
    private final MessageService messageService;
    private final GoogleNLPService googleNLPService;
    private final OpenAiService openAiService;


    // stomp를 사용하여  구독 uri를 통한 메세지 전송시에 session정보가 명시적으로 전달되지않아서 전송시에 대상이되는 session의 정보를 불러오지 못했음..
    // 그래서 세션의 정보를 명시적으로 지정하는 방법을 찾아보니 websocket api만을 이용한 방법이 있었음



    public MyWsHandler(WsSessionManager wsSessionManager, ChatService chatService,MessageService messageService,GoogleNLPService googleNLPService,OpenAiService openAiService) {

        this.googleNLPService = googleNLPService;
        this.messageService = messageService;
        this.chatService  = chatService;
        this.wsSessionManager = wsSessionManager;
        this.openAiService = openAiService;

    }  // 핸들러에서 필요한 의존성 주입 socket정의 클래스에서부터 주입해주어야함.



    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userEm = getUserEmailFromSession(session); // email로 session의 정보를 명시적으로 등록해주니 정상적으로 아주 빠르게 메세지 전달과 읽기가 가능해졌다.
                                                            // 세션이 등록되기전에 beforehandshake에서 jwt를 검증하는 과정에서 검증후 파싱하여 email추출이 가능함
        wsSessionManager.addSession(userEm,session);
        System.out.println("세션이 등록되었습니다. 이메일: " + userEm);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

        JsonObject jsonMessage = JsonParser.parseString(message.getPayload().toString()).getAsJsonObject();
        JsonObject payload = jsonMessage.has("payload") ? jsonMessage.getAsJsonObject("payload") : null;
        String type = jsonMessage.get("type").getAsString();
        String time = LocalTime.now().toString();
        if("message".equals(type)){

            if (payload != null) {
                String destination = jsonMessage.get("destination").getAsString();
                String recipientEmail = destination.substring(destination.lastIndexOf('/') + 1);
                String recipientEmail1 = URLDecoder.decode(recipientEmail, StandardCharsets.UTF_8.name());
                String sender = payload.get("sender").getAsString();
                String content = payload.get("content").getAsString();
                Optional<RoomDTO> roomDTO = chatService.findChatroomByUser(sender, recipientEmail1) ;// 클라이언트 응답 본문이 챗룸에는 다수의 roominuser를 참조 user는 roominuser를 통해 다수의 chatroom과 연결
                                                                                                     // 이때 chatroom 객체의 정보를 json 직렬화시에 roominuser를 통해 user를 연결 user는 또 roominuser를 조회하고 chatroom을 연결
                                                                                                     // 그래서 순환참조가 일어났음... 그래서 userdto chatroomdto 를 만들어서 필요치 않는 관계필드를 제외시키고 직렬화시키니 문제 해결
                                                                                                     // 더 좋은 방법이 있을거같음.
                WebSocketSession targetSession = wsSessionManager.getSession(recipientEmail1);
                if(!roomDTO.isPresent()){
                    RoomDTO roomDTO1 = chatService.createOneOnOneChatRoom(sender,recipientEmail1);
                    JsonObject msgObj2 = new JsonObject();
                    msgObj2.addProperty("type","newOne");
                    msgObj2.addProperty("sender",sender);
                    msgObj2.addProperty("roomId",roomDTO1.getId());
                    session.sendMessage(new TextMessage(msgObj2.toString()));

                    if(targetSession != null){


                        JsonObject msgObj = new JsonObject();
                        msgObj.addProperty("type","message");
                        msgObj.addProperty("roomId",roomDTO1.getId());
                        msgObj.addProperty("sender",sender);
                        msgObj.addProperty("recipient",recipientEmail);
                        msgObj.addProperty("content",content);
                        msgObj.addProperty("time",time);


                        targetSession.sendMessage(new TextMessage(msgObj.toString()));
                        targetSession.sendMessage(new TextMessage(msgObj2.toString()));
                    }
                    messageService.save(roomDTO1.getId(),sender,content);

                } else{
                    if (targetSession != null) {


                        String sentimentResult = openAiService.genRepsAsyn(content); // 기존 googleNLP를 이용하여 긍정부정 강도 점수로 sentiment 객체를 만들어주었는데 이젠 그럴필요없다.
                        JsonObject msgObj = new JsonObject();
                        msgObj.addProperty("type","message");
                        msgObj.addProperty("roomId", roomDTO.get().getId()); // roomId를 전달하는 이유는 메세지 출력시에 각기 다른 채팅방의 메세지가 채팅방에 모두 출력되는 문제가 생김
                        msgObj.addProperty("sender" , sender);
                        msgObj.addProperty("content" , content);
                        msgObj.addProperty("recipient" , recipientEmail);
                        msgObj.addProperty("time" , time);
                        msgObj.addProperty("sentiment" , sentimentResult); // 클라이언트가 받게될 jsonobj 에다가 gpt의 결과 문자열 넣고 클라이언트에서 파싱하면 끝.

                        // 여기서 감정분석의 결과 까지 db에 저장을 할지 말지 고민... 사실 이미 message 객체를 위에서 만들어주고있는 것 자체로도 많은 트랜잭션이 발생하게되니까 일단 생략..
                        // targetSession의 존재 유무에 따라서 저장을 하는게 나을지도 모르겠다. 예를 들어 메세지를 받게될 대상 session이 존재한다면 트랜잭션을 멈추고 해당 세션의 종료시에 메세지 객체를 저장후 asc로 order by해준다?
                        // 이것도 좀 문제인듯 만약 해당 채팅방말고 다른 채팅방을 클릭시에 저장이되지 않았으니 아직 이전 메세지들을 확인이 불가하다.
                        // 일단 보낼 msg마다 저장해보고 배포 후에 얼마나 문제가 될지 생각해보자.
                        targetSession.sendMessage(new TextMessage(msgObj.toString()));
                        roomDTO.ifPresent(room -> {

                            Long roomId =room.getId();
                            messageService.save(roomId,sender,payload.get("content").getAsString());
                        });
                        System.out.println("메시지를 전송했습니다. 수신자 이메일: " + recipientEmail1);
                    } else {
                        System.out.println("대상 세션을 찾을 수 없습니다. 수신자 이메일: " + recipientEmail1);
                    }

                }
            } else {
                // payload가 없을 경우 처리
                System.out.println("메시지 payload가 존재하지 않습니다.");
            }
        } else if ("enterRoom".equals(type)) { // 세션이 채팅방에 입장...
            JsonObject msgObj = JsonParser.parseString(message.getPayload().toString()).getAsJsonObject();
            System.out.println(msgObj);
            Long roomId = msgObj.get("roomId").getAsLong();
            String sender = msgObj.get("sender").getAsString();
            String receiver = msgObj.get("receiver").getAsString();
            WebSocketSession trgtSession = wsSessionManager.getSession(receiver); // 채팅방 입장시 msg 객체는 receiver도 포함되어서 전송됨. 그 점을 이용하여 세션을 찾는다.
            boolean isSessioninRoom = wsSessionManager.isSessionInRoom(roomId,trgtSession);
            Optional<RoomDTO> roomDTO = chatService.findChatroomByUser(sender, receiver) ;
            wsSessionManager.addRoomSession(roomId,session);
            System.out.println("채팅방 접속 : " + session.getId());
               if(trgtSession != null && isSessioninRoom ){
                   System.out.println("세션이 채팅방에 존재합니다.");  // 이 때부터 db차원에서 읽음 안읽음을 추가해줘야할거같음
                   JsonObject response = new JsonObject();
                   response.addProperty("type","status");
                   response.addProperty("roomId",roomId);
                   response.addProperty("doesSheInRoom", true);  // boolean 값을 클라이언트로 전달

                   session.sendMessage(new TextMessage(response.toString()));
                   trgtSession.sendMessage(new TextMessage(response.toString()));

               }else{
                   JsonObject response = new JsonObject();
                   response.addProperty("type","status");
                   response.addProperty("roomId",roomId);
                   response.addProperty("doesSheInRoom", false);

                   session.sendMessage(new TextMessage(response.toString()));
                   System.out.println("접속한 상대 세션 없음");
               }


            
        } else if ("close".equals(type)) {  // 상대방이 채팅방을 닫기 버튼을 눌렀을때 전송되는 메세지의타입 = close 라서
            JsonObject msgObj = JsonParser.parseString(message.getPayload().toString()).getAsJsonObject();
            Long roomId = msgObj.get("roomId").getAsLong();
            String sem = msgObj.get("semail").getAsString(); // 채팅방 닫기를 누른 사람.
            JsonObject response = new JsonObject();
            response.addProperty("type","status");
            response.addProperty("roomId",roomId);
            response.addProperty("doesSheInRoom", false);
            wsSessionManager.removeSessionFromRoom(roomId,sem);
            System.out.println("룸에서 현재 세션 삭제"  +sem);
            if(wsSessionManager.getRoomSession(roomId)){
                WebSocketSession trgtSession = wsSessionManager.getSessionFromRoom(roomId);
                trgtSession.sendMessage(new TextMessage(response.toString()));
                return;
            }



        }


    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String userEmail = getUserEmailFromSession(session);
        wsSessionManager.removeSession(userEmail);
        System.out.println("세션이 종료되었습니다. 이메일: " + userEmail);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


    private String getUserEmailFromSession(WebSocketSession session) {
        // 예시: 헤더에서 이메일을 추출하거나 토큰을 통해 이메일을 찾는 방식 구현
        String userEmail = (String) session.getAttributes().get("userEmail");  // 실제 구현에서 이메일 추출 방법을 작성해야 합니다.
        return userEmail;
    }

}
