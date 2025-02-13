package com.collabdoc.project.service;

import com.collabdoc.project.model.CollabDoc;
import com.collabdoc.project.model.CRDTCharacter;
import com.collabdoc.project.repository.mongo.CollabDocMongoRepository;
import com.collabdoc.project.repository.sql.CollabDocSqlRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollabDocService {

    @Value("${db.type}")
    private String dbType;

    @Autowired(required = false)
    private CollabDocMongoRepository mongoRepository;

    @Autowired(required = false)
    private CollabDocSqlRepository sqlRepository;

    private boolean isMongoDb() {
        return "mongodb".equalsIgnoreCase(dbType);
    }

    /** ✅ Create a New Snippet */
    public CollabDoc createSnippet(CollabDoc collabDoc) {
        if (collabDoc.getCreatedAt() == null) {
            collabDoc.setCreatedAt(LocalDateTime.now());
        }
        if (collabDoc.getUniqueLink() == null || collabDoc.getUniqueLink().isEmpty()) {
            collabDoc.setUniqueLink(UUID.randomUUID().toString());
        }
        if (isMongoDb()) {
            if (mongoRepository != null) {
                return mongoRepository.save(collabDoc);
            }
        } else {
            if (sqlRepository != null) {
                return sqlRepository.save(collabDoc);
            }
        }
        throw new RuntimeException("No active database connection found.");
    }

    /** ✅ Retrieve Snippet */
    public Optional<CollabDoc> getSnippet(String uniqueLink) {
        if (isMongoDb()) {
            if (mongoRepository != null) {
                return mongoRepository.findByUniqueLink(uniqueLink);
            }
        } else {
            if (sqlRepository != null) {
                return sqlRepository.findByUniqueLink(uniqueLink);
            }
        }
        throw new RuntimeException("No active database connection found.");
    }

    /** ✅ Update Snippet Content */
    public boolean updateSnippet(String uniqueLink, List<CRDTCharacter> updatedContent) {
        System.out.println("Updated content: " + updatedContent.stream()
                .map(CRDTCharacter::toString)
                .collect(Collectors.joining(", ")));

        if (isMongoDb()) {
            if (mongoRepository != null) {
                Optional<CollabDoc> optionalSnippet = mongoRepository.findByUniqueLink(uniqueLink);
                if (optionalSnippet.isPresent()) {
                    CollabDoc snippet = optionalSnippet.get();
                    snippet.setContent(updatedContent);
                    mongoRepository.save(snippet);
                    return true;
                }
            }
        } else {
            if (sqlRepository != null) {
                Optional<CollabDoc> optionalSnippet = sqlRepository.findByUniqueLink(uniqueLink);
                if (optionalSnippet.isPresent()) {
                    CollabDoc snippet = optionalSnippet.get();
                    snippet.setContent(updatedContent);
                    sqlRepository.save(snippet);
                    return true;
                }
            }
        }
        return false;
    }
}
