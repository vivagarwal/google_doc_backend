package com.collabdoc.project.service;

import com.collabdoc.project.model.CollabDoc;
import com.collabdoc.project.manager.InMemoryEditManager;
import com.collabdoc.project.model.CRDTCharacter;
import com.collabdoc.project.repository.CRDTCharacterRepository;
import com.collabdoc.project.repository.CollabDocRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Service
public class CollabDocService {

    private final CollabDocRepository collabRepository;
    private final CRDTCharacterRepository crdtCharacterRepository;

    private static final Logger logger = LoggerFactory.getLogger(InMemoryEditManager.class);

    @PersistenceContext
    private EntityManager entityManager;

    public CollabDocService(CollabDocRepository collabRepository,CRDTCharacterRepository crdtCharacterRepository){
        this.collabRepository = collabRepository;
        this.crdtCharacterRepository=crdtCharacterRepository;
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

    // ✅ Initialize sequence values and collabdoc for each character
    for (int i = 0; i < collabDoc.getContent().size(); i++) {
        CRDTCharacter character = collabDoc.getContent().get(i);
        character.setCollabDoc(collabDoc);
        character.setSequence(i);
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

    // ✅ Update snippet content in PostgreSQL
    // note - transactional is needed here as this will also update the crdt character
    @Transactional
    public void saveDocumentandDeleteChars(CollabDoc collabDoc) {
        logger.info("📌 Before Saving: Checking IDs in collabDoc.getContent()");
    
        for (CRDTCharacter character : collabDoc.getContent()) {
            logger.info("✅ Character ID: {} Value: {} Managed: {}", 
                character.getUniqueId(), character.getValue(), entityManager.contains(character));
        }

        collabRepository.save(collabDoc);  // ✅ Now save without deleted references

        // Remove deleted characters from DB
        List<String> deletedIds = collabDoc.getDeletedCharacters();
        if (!deletedIds.isEmpty()) { 
            crdtCharacterRepository.deleteAllById(deletedIds);  // Bulk delete in one DB call
            collabDoc.getDeletedCharacters().clear();  // Clear after deletion
        }
    }
}
