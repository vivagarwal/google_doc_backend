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

   @Transactional
    public void reloadandSaveDocument(String uniqueLink, CollabDoc detCollabDoc){
        CollabDoc managedDoc = collabRepository.findByUniqueLink(uniqueLink)
            .orElseThrow(() -> new RuntimeException("Document not found: " + uniqueLink));
        
        managedDoc.getContent().clear();
        for(CRDTCharacter c : detCollabDoc.getContent()){
            c.setCollabDoc(managedDoc);
            managedDoc.getContent().add(c);
        }

        collabRepository.save(managedDoc);
    }
}
