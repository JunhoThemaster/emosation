package com.emosation.emosation.websocket;

import com.emosation.emosation.Util.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.http.WebSocketHandshakeException;
import java.util.Map;

@Component
public class WebSocketHandShakeInterCeptor implements HandshakeInterceptor {


    private final JwtUtil jwtUtil;
    private final WsSessionManager wsSessionManager;

    public WebSocketHandShakeInterCeptor(JwtUtil jwtUtil,WsSessionManager wsSessionManager) {
        this.wsSessionManager = wsSessionManager;
        this.jwtUtil = jwtUtil;
    }


    @Override //beforeHandShake 메소드 오버라이딩 원래 사전정의된 핸드세이크 메소드에는 아무 내용도 있지않음. 따라서 기존 로그인에서 토큰을 파싱후 해당 유저를 세션에 주입
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = request.getURI().getQuery();  // URL에서 쿼리 파라미터 추출 .. 요거 좀 방식이 참 이전에는 헤더에서 토큰의 값을 추출했는데 지금은 파라미터값으루 추출해야함
                                                    // 이거는 http와 별개로 websocket 프로토콜 자체로 헤더정보가 연결후에는 더이상 존재하지않기 때문인거같음 자세한건 좀더 공부해보자.
        System.out.println("Authorization Query Parameter: " + token);

        if (token != null && token.startsWith("Authorization=Bearer ")) {
            token = token.substring("Authorization=Bearer ".length()); // "Authorization=Bearer " 부분을 제거하여 토큰만 추출함

            if (token != null && !jwtUtil.isExp(token)) {   // jwt 검증 메소드 호출
                String userEmail = jwtUtil.extId(token); // JWT에서 이메일 추출
                attributes.put("userEmail", userEmail);
                System.out.println(userEmail);// WebSocket session에 사용자 정보 저장
                return true; // 핸드셰이크 성공
            }
        }
        return false; // 핸드셰이크 실패
    }

    @Override
    public void afterHandshake(org.springframework.http.server.ServerHttpRequest request, org.springframework.http.server.ServerHttpResponse response, org.springframework.web.socket.WebSocketHandler wsHandler, Exception exception) {

    }
}
