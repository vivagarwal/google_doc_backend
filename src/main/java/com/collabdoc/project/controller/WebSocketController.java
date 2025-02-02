package com.collabdoc.project.controller;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.collabdoc.project.service.CollabDocService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
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
            return "";
        }

        // Check for whitespace-only content
        if (updatedContent.trim().isEmpty()) {
            logger.info("Received whitespace-only content. Broadcasting empty content.");
            return "";
        }

        logger.info("Received update for snippet: '{}'", updatedContent);

        inMemoryEdits.put(uniqueLink, updatedContent);
        return updatedContent;
    }

    @Scheduled(fixedRate = 15*1000) //5 secs
    public void persistEditsPeriodically() {
        System.out.println("Called scheduled");
        inMemoryEdits.forEach((uniqueLink, content) -> {
            // System.out.println(uniqueLink+":\t"+content);
            boolean flag = collabDocService.updateSnippet(uniqueLink, content);
            if(flag)
                logger.info("Persisted snippet {} to database.", uniqueLink);
            else
                logger.info("Persisted not snippet {} to database.", uniqueLink);
        });
        inMemoryEdits.clear();  // Clear memory after persisting
    }
}