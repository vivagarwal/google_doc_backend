package com.collabdoc.project.controller;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.collabdoc.project.model.CRDTCharacter;
import com.collabdoc.project.model.CollabDoc;
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

    private final ConcurrentHashMap<String, CollabDoc> inMemoryEdits = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private CollabDocService collabDocService;

    // @MessageMapping("/snippets/edit/{uniqueLink}")
    // @SendTo("/topic/snippets/{uniqueLink}")
    // public String broadcastSnippetEdit(@Payload(required = false) String updatedContent, @DestinationVariable String uniqueLink) {
    //     if (updatedContent == null) {
    //         logger.info("Received null content. Broadcasting empty content.");
    //         inMemoryEdits.put(uniqueLink, "");
    //         return "";
    //     }

    //     // Check for whitespace-only content
    //     if (updatedContent.trim().isEmpty()) {
    //         logger.info("Received whitespace-only content. Broadcasting empty content.");
    //         inMemoryEdits.put(uniqueLink, "");
    //         return "";
    //     }

    //     logger.info("Received update for snippet: '{}'", updatedContent);

    //     inMemoryEdits.put(uniqueLink, updatedContent);
    //     return updatedContent;
    // }

    @Scheduled(fixedRate = 5*1000) //5 secs
    public void persistEditsPeriodically() {
        // System.out.println("Called scheduled");
        inMemoryEdits.forEach((uniqueLink, document) -> {
            // System.out.println(uniqueLink+":\t"+content);
            boolean flag = collabDocService.updateSnippet(uniqueLink, document.getContent());
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
        private boolean deleteOperation;

        public boolean getDeleteOperation() {
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
    public EditMessage broadcastCharacterEdit(@Payload EditMessage editMessage , @DestinationVariable String uniqueLink) {
        logger.info("[RECEIVED DELTA] Delta: '{}', Position: {}, Session ID: {}, Delete: {}",editMessage.getContentDelta(), editMessage.getCursorPosition(), editMessage.getSessionId(), editMessage.getDeleteOperation());

        // Fetch document from memory or database
        CollabDoc document = inMemoryEdits.computeIfAbsent(uniqueLink, link -> {
            Optional<CollabDoc> doc = collabDocService.getSnippet(uniqueLink);
            return doc.orElseThrow(() -> new RuntimeException("Document not found for uniqueLink: " + uniqueLink));
        });

        // Validate cursor position to prevent out-of-bounds errors
        int position = validatePosition(editMessage.getCursorPosition(), document);

        if (editMessage.getDeleteOperation()) {
            handleDelete(document, position);
        } else {
            handleInsert(document, editMessage.getContentDelta(), position, editMessage.getSessionId());
        }

        return editMessage;
    }

    // Ensure the cursor position is valid within the document size
    private int validatePosition(int position, CollabDoc document) {
        int maxPosition = document.getContent().size();
        return Math.max(0, Math.min(position, maxPosition));
    }

    // Handle insertion of a new CRDT character
    private void handleInsert(CollabDoc document, String delta, int position, String sessionId) {
        String uniqueId = System.currentTimeMillis() + "_" + sessionId;  // Generate unique ID
        CRDTCharacter newChar = new CRDTCharacter(delta, uniqueId);
        document.getContent().add(position, newChar);  // Insert character
        logger.info("Inserted character '{}' at position {}.", delta, position);
    }

    // Handle deletion by marking the character as logically deleted
    private void handleDelete(CollabDoc document, int position) {
        if (position < document.getContent().size()) {
            CRDTCharacter charToDelete = document.getContent().get(position);
            charToDelete.delete();  // Mark character as deleted
            logger.info("Deleted character '{}' at position {}.", charToDelete.getValue(), position);
        }
    }
        
}