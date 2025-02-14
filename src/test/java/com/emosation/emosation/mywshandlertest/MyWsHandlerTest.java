package com.emosation.emosation.mywshandlertest;


import com.emosation.emosation.websocket.MyWsHandler;
import com.emosation.emosation.websocket.WsSessionManager;
import com.nimbusds.jose.shaded.gson.JsonObject;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MyWsHandlerTest {
//
//
//    @InjectMocks
//    private MyWsHandler myWsHandler;
//
//    @Mock
//    private WebSocketSession senderSession;
//
//    @Mock
//    private WebSocketSession receiverSession;
//
//    @Mock
//    private WebSocketMessage<?> webSocketMessage;
//
//    @Mock
//    private WsSessionManager wsSessionManager;
//
//
//
//
//    @BeforeEach
//    public void setUp(){
//        MockitoAnnotations.openMocks(this);
//
//    }
//
//
//    @Test
//    public void testHandleMessage() throws Exception {
//        String sender = "alice@example.com";
//        String receiver = "bob@example.com";
//        String messageContent = "Hello, World!";
//
//        // senderSession의 'email' 속성 mock
//        when(senderSession.getAttributes()).thenReturn(Map.of("email", sender));
//
//        // 메시지 페이로드 설정
//        WebSocketMessage<String> webSocketMessage = mock(WebSocketMessage.class);  // WebSocketMessage mock 생성
//
//        // 'webSocketMessage.getPayload()'가 제대로 반환되도록 설정
//        String payload = "{\"destination\": \"/app/user/bob@example.com\", \"content\": \"" + messageContent + "\"}";
//        when(webSocketMessage.getPayload()).thenReturn(payload);  // mock된 payload 반환
//
//        // 세션 관리에서 수신자 이메일에 해당하는 세션 반환
//        when(wsSessionManager.getSession(receiver)).thenReturn(receiverSession);
//
//        // receiverSession에서 sendMessage가 호출될 때 확인할 수 있도록 mock
//        doNothing().when(receiverSession).sendMessage(any(TextMessage.class));
//
//        // addSession 메서드를 통해 세션 등록
//        doNothing().when(wsSessionManager).addSession(sender, senderSession);
//        doNothing().when(wsSessionManager).addSession(receiver, receiverSession);
//
//        // 실제 메시지 처리 호출
//        myWsHandler.handleMessage(senderSession, webSocketMessage);
//
//        // 세션이 WsSessionManager에 등록되었는지 확인(이부분 실제로 등록은 안됨 세션자체를 등록하기전에 핸드쉐이크로 토큰을 검증해야하기때문)
//        verify(wsSessionManager).addSession(sender, senderSession);  // 발신자 세션 등록 확인
//        verify(wsSessionManager).addSession(receiver, receiverSession);  // 수신자 세션 등록 확인
//
//        // 메시지 전송이 수신자 세션에서 sendMessage로 호출되었는지 확인
//        verify(receiverSession).sendMessage(new TextMessage("메세지: " + messageContent));
//    }
//









}
