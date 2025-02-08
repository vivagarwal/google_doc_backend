package com.collabdoc.project.manager;

import com.collabdoc.project.model.EditMessage;
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

    public void addOrUpdateEdit(String uniqueLink, EditMessage editMessage) {
        CollabDoc document = inMemoryEdits.get(uniqueLink);
        if (editMessage.getDeleteOperation()) {
            document.handleDelete(editMessage.getCursorPosition());
        } else {
            document.handleInsert(editMessage.getContentDelta(), editMessage.getCursorPosition(), editMessage.getSessionId());
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
        //inMemoryEdits.clear();
    }

    public boolean persistEditsforOne(String uniqueLink){
        CollabDoc document = inMemoryEdits.get(uniqueLink);
        boolean isUpdated = collabDocService.updateSnippet(uniqueLink, document.getContent());
        if(isUpdated)
            return true;
        else   
            return false;
    }

    public void loadinMemory(String uniqueLink){
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
    }
}
