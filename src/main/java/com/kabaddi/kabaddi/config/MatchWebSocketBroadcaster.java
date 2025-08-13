package com.kabaddi.kabaddi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchWebSocketBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendScoreUpdate(String matchId, Object payload) {
        log.info("Broadcasting to /topic/match/{} payload: {}", matchId, payload);
        messagingTemplate.convertAndSend("/topic/match/" + matchId, payload);
    }
    public void sendUserScoreUpdate(String userId, Object payload) {
        log.info("Broadcasting to /topic/user/{}/score payload: {}", userId, payload);
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/score", payload);
    }


}

