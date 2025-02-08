package com.collabdoc.project.controller;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import com.collabdoc.project.manager.InMemoryEditManager;
import com.collabdoc.project.model.EditMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    private final ConcurrentHashMap<String, String> sessionToDocMap = new ConcurrentHashMap<>();


    @Autowired
    private InMemoryEditManager inMemoryEditManager;

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = (String) ((GenericMessage) event.getMessage()).getHeaders().get("simpSessionId");
        logger.info("User disconnected. Session ID: {}", sessionId);
        String uniqueLink = sessionToDocMap.remove(sessionId);
        if (uniqueLink != null) {
            logger.info("User disconnected. Session ID: {}, Document: {}", sessionId, uniqueLink);
            inMemoryEditManager.decrementConnectedClients(uniqueLink);
        }

        //not needed now as all user requests are routed through in-memory route
        // inMemoryEditManager.persistEditsPeriodically();
    }

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        String uniqueLink = (String) headerAccessor.getNativeHeader("uniqueLink").get(0);

        // Map sessionId to uniqueLink
        sessionToDocMap.put(sessionId, uniqueLink);
        inMemoryEditManager.loadinMemory(uniqueLink);
        inMemoryEditManager.incrementConnectedClients(uniqueLink);
        logger.info("User connected. Session ID: {}, Document: {}", sessionId, uniqueLink);
    }


    @MessageMapping("/snippets/edit-delta/{uniqueLink}")
    @SendTo("/topic/snippets-delta/{uniqueLink}")
    public EditMessage broadcastCharacterEdit(@Payload EditMessage editMessage , @DestinationVariable String uniqueLink) {
        logger.info("[RECEIVED DELTA] Delta: '{}', Position: {}, Session ID: {}, Delete: {}",editMessage.getContentDelta(), editMessage.getCursorPosition(), editMessage.getSessionId(), editMessage.getDeleteOperation());
        inMemoryEditManager.addOrUpdateEdit(uniqueLink,editMessage);
        return editMessage;
    }     
}