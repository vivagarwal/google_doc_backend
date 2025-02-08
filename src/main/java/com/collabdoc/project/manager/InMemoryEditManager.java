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
    private final ConcurrentHashMap<String, CollabDocState> inMemoryEdits = new ConcurrentHashMap<>();

    @Autowired
    private CollabDocService collabDocService;

    public void addOrUpdateEdit(String uniqueLink, EditMessage editMessage) {
        CollabDocState collabDocState = inMemoryEdits.get(uniqueLink);
        collabDocState.setDoc_changed_flag(true);
        CollabDoc document = collabDocState.getCollabDoc();
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
        inMemoryEdits.forEach((uniqueLink, collabDocState) -> {
            if(collabDocState.isDoc_changed_flag()){
                boolean isUpdated = collabDocService.updateSnippet(uniqueLink, collabDocState.getCollabDoc().getContent());
                if (isUpdated) {
                    logger.info("Successfully persisted document '{}'.", uniqueLink);
                    collabDocState.setDoc_changed_flag(false);
                } else {
                    logger.error("Failed to persist document '{}'.", uniqueLink);
                }
            }
        });

        // Clear the in-memory edits after persistence
        //inMemoryEdits.clear();
    }

    public boolean persistEditsforOne(String uniqueLink){
        CollabDoc document = inMemoryEdits.get(uniqueLink).getCollabDoc();
        boolean isUpdated = collabDocService.updateSnippet(uniqueLink, document.getContent());
        if(isUpdated)
            return true;
        else   
            return false;
    }

    public void loadinMemory(String uniqueLink){
        CollabDocState collabDocState = inMemoryEdits.get(uniqueLink);
        if (collabDocState == null) {
            Optional<CollabDoc> docFromDB = collabDocService.getSnippet(uniqueLink);
            if (docFromDB.isPresent()) {
                CollabDoc document = docFromDB.get();
                collabDocState=new CollabDocState(document,false);
                inMemoryEdits.put(uniqueLink, collabDocState);
            } else {
                throw new RuntimeException("Document not found for uniqueLink: " + uniqueLink);
            }
        }
    }

    public CollabDoc viewDoc(String uniqueLink){
        CollabDoc document = inMemoryEdits.get(uniqueLink).getCollabDoc();
        return document;  
    }
}
