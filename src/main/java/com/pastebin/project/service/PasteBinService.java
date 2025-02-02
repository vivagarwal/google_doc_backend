package com.pastebin.project.service;

import com.pastebin.project.model.PasteBin;
import com.pastebin.project.repository.PasteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasteBinService {

    private final PasteRepository pasteRepository;

    public PasteBinService(PasteRepository pasteRepository){
        this.pasteRepository=pasteRepository;
    }

    // Create a new snippet
    public PasteBin createSnippet(PasteBin pasteBin) {
        if (pasteBin.getCreatedAt() == null) {
            pasteBin.setCreatedAt(LocalDateTime.now());
        }
        if (pasteBin.getUniqueLink() == null || pasteBin.getUniqueLink().isEmpty()) {
            pasteBin.setUniqueLink(UUID.randomUUID().toString());
        }
        return pasteRepository.save(pasteBin);
    }

    // Retrieve and check expiration/views
    public Optional<PasteBin> getSnippet(String uniqueLink) {
        Optional<PasteBin> snippet = pasteRepository.findByUniqueLink(uniqueLink);

        if (snippet.isPresent()) {
            // Check expiration
            if (snippet.get().getExpirationTime() != null &&
                    snippet.get().getExpirationTime().isBefore(LocalDateTime.now())) {
                pasteRepository.delete(snippet.get());
                return Optional.empty();
            }

            // Check view limit
            if (snippet.get().getAccessLimit() != null &&
                    snippet.get().getCurrentViews() >= snippet.get().getAccessLimit()) {
                pasteRepository.delete(snippet.get());
                return Optional.empty();
            }

            // Increment views and save
            snippet.get().setCurrentViews(snippet.get().getCurrentViews() + 1);
            pasteRepository.save(snippet.get());
        }
        return snippet;
    }

    public boolean updateSnippet(String uniqueLink, String content) {
        // System.out.println("Attempting to update snippet with uniqueLink: {}  "  + uniqueLink);

        // Find the snippet by uniqueLink
        Optional<PasteBin> optionalSnippet = pasteRepository.findByUniqueLink(uniqueLink);

        if (optionalSnippet.isPresent()) {
            // System.out.println("Snippet found with uniqueLink: {}" + uniqueLink);

            PasteBin snippet = optionalSnippet.get();
            // logger.debug("Current content: {}", snippet.getContent());

            snippet.setContent(content);  // Update the content
            pasteRepository.save(snippet);  // Save the updated snippet to the database

            // logger.debug("Updated snippet saved with new content: {}", content);
            return true;
        } else {
            // System.out.println("Snippet not found with uniqueLink: {}"+ uniqueLink);
            return false;
        }
    }

}
