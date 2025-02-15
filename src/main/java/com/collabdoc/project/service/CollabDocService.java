package com.collabdoc.project.service;

import com.collabdoc.project.model.CollabDoc;
import com.collabdoc.project.model.CRDTCharacter;
import com.collabdoc.project.repository.CRDTCharacterRepository;
import com.collabdoc.project.repository.CollabDocRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
public class CollabDocService {

    private final CollabDocRepository collabRepository;
    private final CRDTCharacterRepository crdtCharacterRepository;

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

    // ✅ Ensure all CRDTCharacters are linked to this CollabDoc
    for (CRDTCharacter character : collabDoc.getContent()) {
        character.setCollabDoc(collabDoc);
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
    @Transactional
    public boolean updateSnippet(String uniqueLink, List<CRDTCharacter> updatedContent) {
        Optional<CollabDoc> optionalSnippet = collabRepository.findByUniqueLink(uniqueLink);
        
        if (optionalSnippet.isPresent()) {
            CollabDoc snippet = optionalSnippet.get();

            // ✅ Clear existing content and re-add new content
            snippet.getContent().clear();
            for (CRDTCharacter character : updatedContent) {
                character.setCollabDoc(snippet); // ✅ Ensure foreign key reference
            }
            snippet.getContent().addAll(updatedContent);

            collabRepository.save(snippet); // Save updates
            return true;
        }
        return false;
    }
}
