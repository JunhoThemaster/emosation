package com.emosation.emosation.websocket;


import com.emosation.emosation.Util.JwtUtil;
import com.emosation.emosation.sevices.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final JwtUtil jwtUtil;
    private final WsSessionManager wsSessionManager;
    private final ChatService chatService;

//    private final GoogleNLPService googleNLPService;

    private final RedisChatService redisChatService;
    private final OpenAiService openAiService;
    private final RedisMessageService redisMessageService;
    private final RedisSessionService redisSessionService;
    private final UserService userService;

    @Autowired
    public WebSocketConfig(JwtUtil jwtUtil,WsSessionManager wsSessionManager,
                           ChatService chatService,OpenAiService openAiService,
                           RedisMessageService redisMessageService,RedisSessionService redisSessionService,
                           RedisChatService redisChatService,UserService userService) {
        this.chatService = chatService;
        this.wsSessionManager = wsSessionManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.redisChatService = redisChatService;
        this.openAiService = openAiService;
        this.redisMessageService = redisMessageService;
        this.redisSessionService = redisSessionService;
    }





    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MyWsHandler(wsSessionManager,chatService,redisChatService,openAiService,redisMessageService,redisSessionService,userService),"/ws-login") .setAllowedOrigins("https://13.125.58.11:8443")  .addInterceptors(new WebSocketHandShakeInterCeptor(jwtUtil, wsSessionManager));
        // 배포시에는 origins 값을 변경해줘야함.
        // 추후 고려해야할점.  aws ec2인스턴스에 배포를 하게될텐데 배포시에는 인스턴스의 시작과 종료 주기마다 도메인이될 ip주소가 동적으로 바뀌게될거임 그래서 허용 도메인 주소를 인스턴스 시작마다 바꿔야하나?
        // 이건 좀 아닌듯함. 공인 ip가 바뀌지않고 static하게 만드려면 elastic ip를 사용하면 된다고 한다. 더 나아가 route53 에서 도메인 이름 까지 지정가능하다고 하니 프로젝트 막바지 단계에 고려해보자.

    } // 의존성 주입이 참
}
