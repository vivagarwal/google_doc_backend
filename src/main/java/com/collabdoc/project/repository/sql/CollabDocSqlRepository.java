package com.collabdoc.project.repository.sql;

import com.collabdoc.project.model.CollabDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CollabDocSqlRepository extends JpaRepository<CollabDoc, String> {
    Optional<CollabDoc> findByUniqueLink(String uniqueLink);
}
