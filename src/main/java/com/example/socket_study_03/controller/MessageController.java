package com.example.socket_study_03.controller;

import com.example.socket_study_03.domain.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final SimpMessageSendingOperations simpMessageSendingOperations;

    //hello로 들어오는 메세지에 대해서 처리하는 로직
    //sub/channel/ {구독된 ID} 로 메세지를 보내면 구독자에게 보내준다.
    @MessageMapping("/hello")
    public void message(WebSocketMessage webSocketMessage){
        simpMessageSendingOperations.convertAndSend("/sub/channel/"+ webSocketMessage.getChannelId(), webSocketMessage.getSender()+" "+webSocketMessage.getData());
        
    }
}
