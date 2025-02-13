package com.collabdoc.project.service;

import com.collabdoc.project.model.CollabDoc;
import com.collabdoc.project.model.CRDTCharacter;
import com.collabdoc.project.repository.CollabDocRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class CollabDocService {

    private final CollabDocRepository collabRepository;

    public CollabDocService(CollabDocRepository collabRepository){
        this.collabRepository=collabRepository;
    }

    // Create a new snippet
    public CollabDoc createSnippet(CollabDoc collabDoc) {
        if (collabDoc.getCreatedAt() == null) {
            collabDoc.setCreatedAt(LocalDateTime.now());
        }
        if (collabDoc.getUniqueLink() == null || collabDoc.getUniqueLink().isEmpty()) {
            collabDoc.setUniqueLink(UUID.randomUUID().toString());
        }
        return collabRepository.save(collabDoc);
    }

    // Retrieve and check expiration/views
    public Optional<CollabDoc> getSnippet(String uniqueLink) {
        return collabRepository.findByUniqueLink(uniqueLink);
    }

    public boolean updateSnippet(String uniqueLink, List<CRDTCharacter> updatedContent) {
        // System.out.println("updated content : " + " " + updatedContent);
        System.out.println(
            updatedContent.stream()
                        .map(CRDTCharacter::toString)
                        .collect(Collectors.joining(", ")));
         Optional<CollabDoc> optionalSnippet = collabRepository.findByUniqueLink(uniqueLink);
        if (optionalSnippet.isPresent()) {
            // System.out.println("Snippet found with uniqueLink: {}" + uniqueLink);
            CollabDoc snippet = optionalSnippet.get();
            // logger.debug("Current content: {}", snippet.getContent());
            snippet.setContent(updatedContent);  // Update the content
            collabRepository.save(snippet);  // Save the updated snippet to the database
            // logger.debug("Updated snippet saved with new content: {}", content);
            return true;
        } else {
            // System.out.println("Snippet not found with uniqueLink: {}"+ uniqueLink);
            return false;
        }
    }
}