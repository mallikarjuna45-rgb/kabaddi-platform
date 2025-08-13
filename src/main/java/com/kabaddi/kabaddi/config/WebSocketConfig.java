package com.kabaddi.kabaddi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // For clients to subscribe
        config.setApplicationDestinationPrefixes("/app"); // For clients to send messages to server
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Raw WebSocket endpoint — good for testing with tools like Postman, websocket clients
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        // 2. SockJS endpoint — for React frontend or any browser clients (better fallback support)
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

}
