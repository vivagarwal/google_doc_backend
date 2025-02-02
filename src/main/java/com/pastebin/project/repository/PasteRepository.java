package com.pastebin.project.repository;

import com.pastebin.project.model.PasteBin;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasteRepository extends MongoRepository<PasteBin,String> {
    Optional<PasteBin> findByUniqueLink(String uniqueLink);

}
