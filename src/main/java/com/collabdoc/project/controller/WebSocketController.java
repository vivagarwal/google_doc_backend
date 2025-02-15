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

import java.util.List;
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
        String sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");

        if (sessionId != null) {
            logger.info("User disconnected. Session ID: {}", sessionId);
            String uniqueLink = sessionToDocMap.remove(sessionId);

            if (uniqueLink != null) {
                logger.info("User disconnected. Session ID: {}, Document: {}", sessionId, uniqueLink);
                inMemoryEditManager.decrementConnectedClients(uniqueLink);
            }
        } else {
            logger.warn("SessionDisconnectEvent received but sessionId is null!");
        }
    }

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        List<String> uniqueLinkHeaders = headerAccessor.getNativeHeader("uniqueLink");

        // ✅ Prevent NullPointerException if "uniqueLink" header is missing
        if (uniqueLinkHeaders == null || uniqueLinkHeaders.isEmpty()) {
            logger.error("Missing 'uniqueLink' header for WebSocket connection. Rejecting session: {}", sessionId);
            return;
        }

        String uniqueLink = uniqueLinkHeaders.get(0); // Extract the uniqueLink value

        // Map sessionId to uniqueLink
        sessionToDocMap.put(sessionId, uniqueLink);
        inMemoryEditManager.loadinMemory(uniqueLink);
        inMemoryEditManager.incrementConnectedClients(uniqueLink);
        logger.info("User connected. Session ID: {}, Document: {}", sessionId, uniqueLink);
    }

    @MessageMapping("/snippets/edit-delta/{uniqueLink}")
    @SendTo("/topic/snippets-delta/{uniqueLink}")
    public EditMessage broadcastCharacterEdit(@Payload EditMessage editMessage, @DestinationVariable String uniqueLink) {
        logger.info("[RECEIVED DELTA] Delta: '{}', Position: {}, Session ID: {}, Delete: {}",
                editMessage.getContentDelta(), editMessage.getCursorPosition(), editMessage.getSessionId(), editMessage.getDeleteOperation());

        inMemoryEditManager.addOrUpdateEdit(uniqueLink, editMessage);
        return editMessage;
    }
}
