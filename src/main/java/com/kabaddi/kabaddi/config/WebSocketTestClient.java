//package com.kabaddi.kabaddi.config;
//
//import org.springframework.messaging.converter.MappingJackson2MessageConverter;
//import org.springframework.messaging.simp.stomp.*;
//import org.springframework.web.socket.WebSocketHttpHeaders;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//import org.springframework.web.socket.messaging.WebSocketStompClient;
//
//import java.lang.reflect.Type;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//public class WebSocketTestClient {
//
//    private static final String WS_URL = "ws://localhost:8080/ws";
//    private static final String MATCH_TOPIC = "/topic/match/6899bb91bccd8b42c7c4977f";
//    private static final String USER_TOPIC = "/topic/user/6899ab84b72a0cf73c0828c9/score";
//
//    public static void main(String[] args) throws Exception {
//        CountDownLatch latch = new CountDownLatch(2); // Wait for both messages
//
//        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
//        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//
//        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
//            @Override
//            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
//                System.out.println("✅ Connected to WebSocket");
//
//                // Subscribe to match updates
//                session.subscribe(MATCH_TOPIC, new StompFrameHandler() {
//                    @Override
//                    public Type getPayloadType(StompHeaders headers) {
//                        return byte[].class;
//                    }
//
//                    @Override
//                    public void handleFrame(StompHeaders headers, Object payload) {
//                        String json = new String((byte[]) payload);
//                        System.out.println("📢 Match update: " + json);
//                        latch.countDown();
//                    }
//                });
//
//                // Subscribe to user score updates
//                session.subscribe(USER_TOPIC, new StompFrameHandler() {
//                    @Override
//                    public Type getPayloadType(StompHeaders headers) {
//                        return byte[].class;
//                    }
//
//                    @Override
//                    public void handleFrame(StompHeaders headers, Object payload) {
//                        String json = new String((byte[]) payload);
//                        System.out.println("👤 User score update: " + json);
//                        latch.countDown();
//                    }
//                });
//            }
//
//            @Override
//            public void handleTransportError(StompSession session, Throwable exception) {
//                System.err.println("❌ Transport error: " + exception.getMessage());
//            }
//        };
//
//        stompClient.connect(WS_URL, new WebSocketHttpHeaders(), sessionHandler);
//        latch.await(60, TimeUnit.SECONDS); // Wait for messages
//    }
//}
