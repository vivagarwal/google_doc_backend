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

}
