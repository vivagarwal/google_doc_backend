package com.collabdoc.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.collabdoc.project.service.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    // Store in-memory document edits temporarily
    private final ConcurrentHashMap<String, String> inMemoryEdits = new ConcurrentHashMap<>();

    @Autowired
    private CollabDocService collabDocService;

    @MessageMapping("/snippets/edit/{uniqueLink}")
    @SendTo("/topic/snippets/{uniqueLink}")
    public String broadcastSnippetEdit(@Payload(required = false) String updatedContent) {
        if (updatedContent == null || updatedContent.trim().isEmpty()) {
            logger.info("Received null or whitespace-only content. Broadcasting empty content.");
            return "";
        }
        logger.info("Received update for snippet: '{}'", updatedContent);
        // Store the latest content in memory for the specific document
        inMemoryEdits.put("uniqueLink", updatedContent);
        return updatedContent;
    }

    // Persist in-memory edits every 5 seconds
    @Scheduled(fixedRate = 5000)
    public void persistEditsPeriodically() {
        inMemoryEdits.forEach((uniqueLink, content) -> {
            collabDocService.updateSnippet(uniqueLink, content);
            logger.info("Persisted snippet {} to database.", uniqueLink);
        });
        inMemoryEdits.clear();  // Clear memory after persisting
    }
}
