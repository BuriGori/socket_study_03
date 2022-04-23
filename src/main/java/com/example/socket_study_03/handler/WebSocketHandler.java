package com.example.socket_study_03.handler;

import com.example.socket_study_03.domain.WebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    //웹소켓을 통신이 가능한 세션들을 모아놓을 Map
    //session ID를 키로 잡고 안에는 session값을 갖는다.
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session){

        // session의 id는 UUID로 만들어 주는 것을 확인할 수 있다?
        var sessionId = session.getId();
        sessions.put(sessionId,session);
        log.info("session id = {} , session = {}",sessionId,session.toString());

        WebSocketMessage message = WebSocketMessage.builder()
                .sender(sessionId)
                .channelId("all")
                .build();
        message.newConnect();

        sessions.values().forEach(s ->{
            try {
                if (!s.getId().equals(sessionId)) {
                    s.sendMessage(new TextMessage(message.toString()));
                }
            }
            catch (Exception e){
                log.info("connection error");
            }
        });
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        String payload = textMessage.getPayload();
        log.info(textMessage.getPayload());
        log.info(textMessage.toString());
        log.info(textMessage.getClass().toString());
        WebSocketMessage message = objectMapper.readValue(payload, WebSocketMessage.class);

        WebSocketSession reciever = sessions.get(message.getChannelId());
        if (reciever != null && reciever.isOpen()) {
            reciever.sendMessage(new TextMessage(message.getData().toString()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        var sessionId = session.getId();
        sessions.remove(sessionId);

        final WebSocketMessage message = new WebSocketMessage();
        message.closeConnect();
        message.setSender(sessionId);

        sessions.values().forEach(s->{
            try {
                s.sendMessage(new TextMessage(message.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
