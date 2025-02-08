package com.collabdoc.project.controller;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.collabdoc.project.manager.InMemoryEditManager;
import com.collabdoc.project.model.CRDTCharacter;
import com.collabdoc.project.model.CollabDoc;
import com.collabdoc.project.model.EditMessage;
import com.collabdoc.project.service.CollabDocService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;


@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private InMemoryEditManager inMemoryEditManager;
    private EditMessage editMessage;

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = (String) ((GenericMessage) event.getMessage()).getHeaders().get("simpSessionId");
        logger.info("User disconnected. Session ID: {}", sessionId);
        inMemoryEditManager.persistEditsPeriodically();
    }


    @MessageMapping("/snippets/edit-delta/{uniqueLink}")
    @SendTo("/topic/snippets-delta/{uniqueLink}")
    public EditMessage broadcastCharacterEdit(@Payload EditMessage editMessage , @DestinationVariable String uniqueLink) {
        logger.info("[RECEIVED DELTA] Delta: '{}', Position: {}, Session ID: {}, Delete: {}",editMessage.getContentDelta(), editMessage.getCursorPosition(), editMessage.getSessionId(), editMessage.getDeleteOperation());
        inMemoryEditManager.addOrUpdateEdit(uniqueLink,editMessage);
        return editMessage;
    }     
}