package com.collabdoc.project.manager;

import com.collabdoc.project.model.EditMessage;
import com.collabdoc.project.model.CollabDoc;
import com.collabdoc.project.service.CollabDocService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

@Component
public class InMemoryEditManager {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryEditManager.class);
    public final ConcurrentHashMap<String, CollabDocState> inMemoryEdits = new ConcurrentHashMap<>();

    @Autowired
    private CollabDocService collabDocService;

    public void addOrUpdateEdit(String uniqueLink, EditMessage editMessage) {
        CollabDocState collabDocState = inMemoryEdits.get(uniqueLink);
    
        if (collabDocState == null) {
            logger.error("Document '{}' not found in memory. Cannot update.", uniqueLink);
            return;
        }
    
        collabDocState.setDoc_changed_flag(true);
        CollabDoc document = collabDocState.getCollabDoc();
        int lineNumber = editMessage.getLineNumber();
        int columnNumber = editMessage.getColumnNumber();
        if (editMessage.getDeleteOperation()) {
            document.handleDelete(lineNumber, columnNumber,editMessage.getContentDelta());
        } else {
            document.handleInsert(editMessage.getContentDelta(), lineNumber, columnNumber, editMessage.getSessionId());
        }
    }
    

    // ✅ Periodic persistence of in-memory documents to the database
    @Scheduled(fixedRate = 10 * 1000)  // Runs every 10 seconds (adjust as needed)
    public void persistEditsPeriodically() {
        //logger.info("Persisting in-memory edits to the database.");
        inMemoryEdits.forEach((uniqueLink, collabDocState) -> {
            if (collabDocState.isDoc_changed_flag()) {

                CollabDoc document = collabDocState.getCollabDoc();

                collabDocService.reloadAndSaveDocument(uniqueLink,document);

                collabDocState.setDoc_changed_flag(false);
                logger.info("Successfully persisted document '{}'.", uniqueLink);
            }
        });

        // ✅ Remove documents no longer in use
        inMemoryEdits.entrySet().removeIf(entry -> entry.getValue().getConnected_clients() == 0);
    }

    public boolean persistEditsforOne(String uniqueLink) {
        CollabDocState collabDocState = inMemoryEdits.get(uniqueLink);

        // ✅ Check if document exists before persisting
        if (collabDocState == null) {
            logger.error("Document '{}' not found in memory. Cannot persist.", uniqueLink);
            return false;
        }

        CollabDoc document = collabDocState.getCollabDoc();

        collabDocService.reloadAndSaveDocument(uniqueLink,document);
        return true;
    }

    public void loadinMemory(String uniqueLink) {
        inMemoryEdits.computeIfAbsent(uniqueLink, link -> {
            Optional<CollabDoc> docFromDB = collabDocService.getSnippet(uniqueLink);
            if (docFromDB.isPresent()) {
                CollabDoc document = docFromDB.get();
                logger.info("Document '{}' loaded into memory.", uniqueLink);
                return new CollabDocState(document, false, 0);
            } else {
                logger.error("Document not found for uniqueLink: {}", uniqueLink);
                throw new RuntimeException("Document not found for uniqueLink: " + uniqueLink);
            }
        });
    }

    public List<String> viewOrderedDoc(String uniqueLink) {
        CollabDocState collabDocState = inMemoryEdits.get(uniqueLink);
    
        if (collabDocState == null) {
            logger.error("Document '{}' not found in memory. Returning null.", uniqueLink);
            return null;
        }

        CollabDoc document = collabDocState.getCollabDoc();

        return document.getOrderedRepresentationOfDoc();
    }

    public void decrementConnectedClients(String uniqueLink) {
        CollabDocState collabDocState = inMemoryEdits.get(uniqueLink);
        if (collabDocState != null) {
            int updatedCount = collabDocState.getConnected_clients() - 1;
            collabDocState.setConnected_clients(Math.max(updatedCount, 0)); // Ensure non-negative count
        }
    }

    public void incrementConnectedClients(String uniqueLink) {
        CollabDocState collabDocState = inMemoryEdits.get(uniqueLink);
        if (collabDocState != null) {
            int updatedCount = collabDocState.getConnected_clients() + 1;
            collabDocState.setConnected_clients(updatedCount);
        }
    }
}
