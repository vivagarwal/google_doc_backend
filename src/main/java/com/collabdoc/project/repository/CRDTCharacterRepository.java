package com.collabdoc.project.repository;

import com.collabdoc.project.model.CRDTCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CRDTCharacterRepository extends JpaRepository<CRDTCharacter, String> {
    Optional<CRDTCharacter> findByUniqueId(String uniqueId);
}
