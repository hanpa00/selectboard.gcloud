package com.phan.game.selectboard;

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
    config.enableSimpleBroker("/topic", "/queue");
    config.setApplicationDestinationPrefixes("/app");
    long id = Thread.currentThread().getId();
	System.out.println("ThreadID: " + id + " configureMessageBroker " + config.toString());
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/gs-guide-websocket").withSockJS();
//    registry.addEndpoint("/gs-guide-websocket").withSockJS().setDisconnectDelay(25000L).setHeartbeatTime(10000L);
    long id = Thread.currentThread().getId();
	System.out.println("ThreadID: " + id + " registerStompEndpoints " + registry.toString());
  }

}