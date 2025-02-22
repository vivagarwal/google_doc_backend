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

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.*;

@Component
public class InMemoryEditManager {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryEditManager.class);
    private final ConcurrentHashMap<String, CollabDocState> inMemoryEdits = new ConcurrentHashMap<>();

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
            logger.error("Document '{}' not found in memory. Returning an empty list.", uniqueLink);
            return new ArrayList<>();
        }
    
        CollabDoc document = collabDocState.getCollabDoc();
    
        // ✅ Preserve spaces while reconstructing the document
        Map<Integer, Map<Integer, String>> structuredLines = document.getContent().stream()
            .sorted(Comparator
                .comparingInt(CRDTCharacter::getLineNumber)
                .thenComparingInt(CRDTCharacter::getColumnNumber))
            .collect(Collectors.groupingBy(
                CRDTCharacter::getLineNumber,
                LinkedHashMap::new,
                Collectors.toMap(
                    CRDTCharacter::getColumnNumber,
                    CRDTCharacter::getValue,
                    (existing, replacement) -> existing, // Merge strategy
                    LinkedHashMap::new
                )
            ));
    
        // ✅ Convert to a list of strings ensuring spaces are correctly placed
        List<String> reconstructedLines = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, String>> entry : structuredLines.entrySet()) {
            Map<Integer, String> lineMap = entry.getValue();
    
            int maxColumn = lineMap.keySet().stream().max(Integer::compareTo).orElse(0);
            
            // 🔥 FIX: Initialize `StringBuilder` with mutable spaces
            StringBuilder lineBuilder = new StringBuilder();
            for (int i = 0; i <= maxColumn; i++) {
                lineBuilder.append(" "); // Pre-fill spaces to avoid index errors
            }
    
            // ✅ Insert characters at their correct column positions
            lineMap.forEach((column, value) -> {
                if (value != null && !value.isEmpty()) {  // ✅ Check for null or empty value
                    if (column < lineBuilder.length()) {  // ✅ Avoid out-of-bounds error
                        lineBuilder.setCharAt(column, value.charAt(0));
                    }
                }
            });
            reconstructedLines.add(lineBuilder.toString());
        }
        return reconstructedLines;
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
