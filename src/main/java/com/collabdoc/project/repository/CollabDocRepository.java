package com.collabdoc.project.repository;

import com.collabdoc.project.model.CollabDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CollabDocRepository extends MongoRepository<CollabDoc,String> {
    Optional<CollabDoc> findByUniqueLink(String uniqueLink);

}