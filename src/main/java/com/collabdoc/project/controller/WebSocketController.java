package com.collabdoc.project.controller;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @MessageMapping("/snippets/edit/{uniqueLink}")
    @SendTo("/topic/snippets/{uniqueLink}")
    public String broadcastSnippetEdit(@Payload(required = false) String updatedContent) {
        if (updatedContent == null) {
            logger.info("Received null content. Broadcasting empty content.");
            return "";
        }

        // Check for whitespace-only content
        if (updatedContent.trim().isEmpty()) {
            logger.info("Received whitespace-only content. Broadcasting empty content.");
            return "";
        }

        logger.info("Received update for snippet: '{}'", updatedContent);
        return updatedContent;
    }
}