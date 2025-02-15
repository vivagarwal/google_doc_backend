package com.collabdoc.project.repository;

import com.collabdoc.project.model.CollabDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CollabDocRepository extends JpaRepository<CollabDoc, Long> {
    Optional<CollabDoc> findByUniqueLink(String uniqueLink);
}
