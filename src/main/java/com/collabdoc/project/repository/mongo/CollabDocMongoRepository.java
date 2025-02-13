package com.collabdoc.project.repository.mongo;

import com.collabdoc.project.model.CollabDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface CollabDocMongoRepository extends MongoRepository<CollabDoc, String> {
    Optional<CollabDoc> findByUniqueLink(String uniqueLink);
}
