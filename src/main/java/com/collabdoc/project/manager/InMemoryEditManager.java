package com.collabdoc.project.manager;


import com.collabdoc.project.model.EditMessage;
import com.collabdoc.project.model.CRDTCharacter;
import com.collabdoc.project.model.CollabDoc;
import com.collabdoc.project.service.CollabDocService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryEditManager {
    
    private static final Logger logger = LoggerFactory.getLogger(InMemoryEditManager.class);
    private final ConcurrentHashMap<String, CollabDoc> inMemoryEdits = new ConcurrentHashMap<>();

    @Autowired
    private CollabDocService collabDocService;
    private EditMessage editMessage;

    public void addOrUpdateEdit(String uniqueLink, EditMessage editMessage) {
        CollabDoc document = inMemoryEdits.get(uniqueLink);
        if (document == null) {
            Optional<CollabDoc> docFromDB = collabDocService.getSnippet(uniqueLink);
            if (docFromDB.isPresent()) {
                document = docFromDB.get();
                inMemoryEdits.put(uniqueLink, document);
            } else {
                throw new RuntimeException("Document not found for uniqueLink: " + uniqueLink);
            }
        }

        if (editMessage.getDeleteOperation()) {
            handleDelete(document, editMessage.getCursorPosition());
        } else {
            handleInsert(document, editMessage.getContentDelta(), editMessage.getCursorPosition(), editMessage.getSessionId());
        }

        logger.info("Updated in-memory document for link '{}'.", uniqueLink);

    }

    // Periodic persistence of in-memory documents to the database
    @Scheduled(fixedRate = 10*1000)  // Runs every 10 seconds (adjust as needed)
    public void persistEditsPeriodically() {
        logger.info("Persisting in-memory edits to the database.");
        inMemoryEdits.forEach((uniqueLink, document) -> {
            boolean isUpdated = collabDocService.updateSnippet(uniqueLink, document.getContent());
            if (isUpdated) {
                logger.info("Successfully persisted document '{}'.", uniqueLink);
            } else {
                logger.error("Failed to persist document '{}'.", uniqueLink);
            }
        });

        // Clear the in-memory edits after persistence
        inMemoryEdits.clear();
    }

     // Handle insertion of a new CRDT character
    public void handleInsert(CollabDoc document, String delta, int position, String sessionId) {
        String uniqueId = System.currentTimeMillis() + "_" + sessionId;  // Generate unique ID
        CRDTCharacter newChar = new CRDTCharacter(delta, uniqueId);
        int adjustedPosition = Math.max(0, Math.min(position, document.getContent().size()));
        document.getContent().add(adjustedPosition, newChar);  // Insert character
        logger.info("Inserted character '{}' at position {}.", delta, position);
    }

    // Handle deletion by marking the character as logically deleted
    public void handleDelete(CollabDoc document, int position) {
    // Adjust the position to ensure it's within bounds
    int adjustedPosition = Math.max(0, Math.min(position, document.getContent().size() - 1));
    if (document.getContent().size() > 0 && adjustedPosition < document.getContent().size()) {
        CRDTCharacter charToDelete = document.getContent().get(adjustedPosition);
        document.getContent().remove(adjustedPosition);  // Remove character from list
        logger.info("Deleted character '{}' at adjusted position {}.", charToDelete.getValue(), adjustedPosition);
    } else {
        logger.warn("Attempted to delete at position {}, but it was out of bounds. Skipping deletion.", position);
    }
    }

}
