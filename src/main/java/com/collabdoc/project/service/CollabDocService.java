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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CollabDocService {

    private static final Logger logger = LoggerFactory.getLogger(CollabDocService.class);

    @Value("${db.type}")
    private String dbType;

    @Autowired(required = false)
    private CollabDocMongoRepository mongoRepository;

    @Autowired(required = false)
    private CollabDocSqlRepository sqlRepository;

    private boolean isMongoDb() {
        return "mongodb".equalsIgnoreCase(dbType);
    }
    
    private boolean isSqlDb() {
        return "sql".equalsIgnoreCase(dbType);
    }    

    /** ✅ Ensure Correct Repository is Loaded */
    private void validateRepository() {
        if (isMongoDb() && mongoRepository == null) {
            throw new RuntimeException("❌ MongoDB repository is not initialized. Check MongoDB configuration.");
        }
        if (isSqlDb() && sqlRepository == null) {
            throw new RuntimeException("❌ SQL repository is not initialized. Check PostgreSQL configuration.");
        }
    }


    /** ✅ Create a New Snippet */
    public CollabDoc createSnippet(CollabDoc collabDoc) {
        validateRepository();  // Ensure correct repository is injected
    
        if (collabDoc.getCreatedAt() == null) {
            collabDoc.setCreatedAt(LocalDateTime.now());
        }
        if (collabDoc.getUniqueLink() == null || collabDoc.getUniqueLink().isEmpty()) {
            collabDoc.setUniqueLink(UUID.randomUUID().toString());
        }
    
        logger.info("🔥 Creating Snippet | DB Type: {} | UniqueLink: {}", dbType, collabDoc.getUniqueLink());
    
        if (isMongoDb()) {
            logger.info("✅ Using MongoDB Repository");
            return mongoRepository.save(collabDoc);
        } 
        if (isSqlDb()) {
            logger.info("✅ Using SQL Repository");
            return sqlRepository.save(collabDoc);
        }
    
        throw new RuntimeException("❌ No active database connection found!");
    }

    /** ✅ Retrieve Snippet */
    public Optional<CollabDoc> getSnippet(String uniqueLink) {
        validateRepository();
        
        logger.info("🔍 Fetching Snippet | DB Type: {} | UniqueLink: {}", dbType, uniqueLink);

        if (isMongoDb()) {
            return mongoRepository.findByUniqueLink(uniqueLink);
        } else {
            return sqlRepository.findByUniqueLink(uniqueLink);
        }
    }

    /** ✅ Update Snippet Content */
    public boolean updateSnippet(String uniqueLink, List<CRDTCharacter> updatedContent) {
        validateRepository();

        logger.info("🔍 Updating Snippet | DB Type: {} | UniqueLink: {}", dbType, uniqueLink);
        logger.info("Updated Content: {}", updatedContent.stream()
                .map(CRDTCharacter::toString)
                .collect(Collectors.joining(", ")));

        if (isMongoDb()) {
            Optional<CollabDoc> optionalSnippet = mongoRepository.findByUniqueLink(uniqueLink);
            if (optionalSnippet.isPresent()) {
                CollabDoc snippet = optionalSnippet.get();
                snippet.setContent(updatedContent);
                mongoRepository.save(snippet);
                return true;
            }
        } else {
            Optional<CollabDoc> optionalSnippet = sqlRepository.findByUniqueLink(uniqueLink);
            if (optionalSnippet.isPresent()) {
                CollabDoc snippet = optionalSnippet.get();
                snippet.setContent(updatedContent);
                sqlRepository.save(snippet);
                return true;
            }
        }

        logger.error("❌ Snippet not found in DB | UniqueLink: {}", uniqueLink);
        return false;
    }
}
