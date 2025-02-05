package com.collabdoc.project.service;

import com.collabdoc.project.model.CollabDoc;
import com.collabdoc.project.repository.CollabDocRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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
        Optional<CollabDoc> snippet = collabRepository.findByUniqueLink(uniqueLink);

        if (snippet.isPresent()) {
            collabRepository.save(snippet.get());
        }
        return snippet;
    }

    public boolean updateSnippet(String uniqueLink, String content) {
        // System.out.println("Attempting to update snippet with uniqueLink: {}  "  + uniqueLink);

        // Find the snippet by uniqueLink
        Optional<CollabDoc> optionalSnippet = collabRepository.findByUniqueLink(uniqueLink);

        if (optionalSnippet.isPresent()) {
            // System.out.println("Snippet found with uniqueLink: {}" + uniqueLink);

            CollabDoc snippet = optionalSnippet.get();
            // logger.debug("Current content: {}", snippet.getContent());

            snippet.setContent(content);  // Update the content
            collabRepository.save(snippet);  // Save the updated snippet to the database

            // logger.debug("Updated snippet saved with new content: {}", content);
            return true;
        } else {
            // System.out.println("Snippet not found with uniqueLink: {}"+ uniqueLink);
            return false;
        }
    }

}
