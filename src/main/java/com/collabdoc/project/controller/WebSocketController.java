package com.collabdoc.project.controller;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

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

@Controller
public class WebSocketController {


    private final ConcurrentHashMap<String, String> inMemoryEdits = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private CollabDocService collabDocService;

    @MessageMapping("/snippets/edit/{uniqueLink}")
    @SendTo("/topic/snippets/{uniqueLink}")
    public String broadcastSnippetEdit(@Payload(required = false) String updatedContent, @DestinationVariable String uniqueLink) {
        if (updatedContent == null) {
            logger.info("Received null content. Broadcasting empty content.");
            inMemoryEdits.put(uniqueLink, "");
            return "";
        }

        // Check for whitespace-only content
        if (updatedContent.trim().isEmpty()) {
            logger.info("Received whitespace-only content. Broadcasting empty content.");
            inMemoryEdits.put(uniqueLink, "");
            return "";
        }

        logger.info("Received update for snippet: '{}'", updatedContent);

        inMemoryEdits.put(uniqueLink, updatedContent);
        return updatedContent;
    }

    @Scheduled(fixedRate = 30*1000) //5 secs
    public void persistEditsPeriodically() {
        // System.out.println("Called scheduled");
        inMemoryEdits.forEach((uniqueLink, content) -> {
            // System.out.println(uniqueLink+":\t"+content);
            boolean flag = collabDocService.updateSnippet(uniqueLink, content);
            if(flag)
                logger.info("Persisted snippet {} to the database.", uniqueLink);
            else
                logger.error("Failed to persist snippet {}.", uniqueLink);
        });
        inMemoryEdits.clear();  // Clear memory after persisting
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = (String) ((GenericMessage) event.getMessage()).getHeaders().get("simpSessionId");
        logger.info("User disconnected. Session ID: {}", sessionId);

        persistEditsPeriodically();
    }

    public static class EditMessage {
        private String contentDelta;
        private int cursorPosition;
        private String sessionId;
        private Boolean deleteOperation;

        public Boolean getDeleteOperation() {
            return deleteOperation;
        }

        public void setDeleteOperation(Boolean deleteOperation) {
            this.deleteOperation = deleteOperation;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getContentDelta() {
            return contentDelta;
        }

        public void setContentDelta(String contentDelta) {
            this.contentDelta = contentDelta;
        }

        public int getCursorPosition() {
            return cursorPosition;
        }

        public void setCursorPosition(int cursorPosition) {
            this.cursorPosition = cursorPosition;
        }
    }

    @MessageMapping("/snippets/edit-delta/{uniqueLink}")
    @SendTo("/topic/snippets-delta/{uniqueLink}")
    public EditMessage broadcastCharacterEdit(@Payload EditMessage editMessage) {
        logger.info("[RECEIVED DELTA] Delta: '{}', Position: {}, Session ID: {} , Delete: {}", editMessage.getContentDelta(), editMessage.getCursorPosition(), editMessage.getSessionId(),editMessage.getDeleteOperation());
        return editMessage;
    }
}