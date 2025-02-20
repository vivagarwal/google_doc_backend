package com.collabdoc.project.service;

import com.collabdoc.project.model.CollabDoc;
import com.collabdoc.project.model.CRDTCharacter;
import com.collabdoc.project.repository.CRDTCharacterRepository;
import com.collabdoc.project.repository.CollabDocRepository;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CollabDocService {

    private final CollabDocRepository collabRepository;
    private final CRDTCharacterRepository crdtCharacterRepository;

    public CollabDocService(CollabDocRepository collabRepository, CRDTCharacterRepository crdtCharacterRepository) {
        this.collabRepository = collabRepository;
        this.crdtCharacterRepository = crdtCharacterRepository;
    }

    // ✅ Create a new snippet
    @Transactional
    public CollabDoc createSnippet(CollabDoc collabDoc) {
        System.out.println("Inside create snippet in collabdoc service");
        System.out.println(collabDoc.getContent());

        if (collabDoc.getCreatedAt() == null) {
            collabDoc.setCreatedAt(LocalDateTime.now());
        }
        if (collabDoc.getUniqueLink() == null || collabDoc.getUniqueLink().isEmpty()) {
            collabDoc.setUniqueLink(UUID.randomUUID().toString());
        }

        // ✅ Assign Line & Column Numbers Correctly
        Map<Integer, Integer> columnTracker = new HashMap<>(); // Track column positions per line
        for (CRDTCharacter character : collabDoc.getContent()) {
            int lineNum = character.getLineNumber();
            int colNum = columnTracker.getOrDefault(lineNum, 0);
            character.setColumnNumber(colNum);
            columnTracker.put(lineNum, colNum + 1);

            character.setCollabDoc(collabDoc); // Set reference
        }

        // ✅ Save the collabDoc first to generate an ID
        collabDoc = collabRepository.save(collabDoc);

        // ✅ Save the CRDTCharacter list after associating with CollabDoc ID
        crdtCharacterRepository.saveAll(collabDoc.getContent());

        return collabDoc;
    }

    // ✅ Retrieve snippet by unique link
    public Optional<CollabDoc> getSnippet(String uniqueLink) {
        return collabRepository.findByUniqueLink(uniqueLink);
    }

    // ✅ Reload and Save Document While Preserving Line-Column Order
    @Transactional
    public void reloadAndSaveDocument(String uniqueLink, CollabDoc detCollabDoc) {
        CollabDoc managedDoc = collabRepository.findByUniqueLink(uniqueLink)
                .orElseThrow(() -> new RuntimeException("Document not found: " + uniqueLink));
    
        // ✅ Clear previous content and remove deleted characters
        managedDoc.getContent().clear();
    
        for (CRDTCharacter c : detCollabDoc.getContent()) {
            int lineNum = c.getLineNumber();
            int colNum = c.getColumnNumber();
            
            // Assign column based on insertion order
            c.setColumnNumber(colNum);
            c.setLineNumber(lineNum);
    
            c.setCollabDoc(managedDoc);
            managedDoc.getContent().add(c);
        }
    
        // ✅ Ensure characters remain sorted properly
        managedDoc.getContent().sort(Comparator.comparingInt(CRDTCharacter::getLineNumber)
                                               .thenComparingInt(CRDTCharacter::getColumnNumber));
    
        collabRepository.save(managedDoc);
    }
    
}
