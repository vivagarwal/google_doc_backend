package com.collabdoc.project.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "collab_docs") // For SQL Database
@Document(collection = "collab_docs") // For MongoDB
public class CollabDoc {

    private static final Logger logger = LoggerFactory.getLogger(CollabDoc.class);

    @Id
    @jakarta.persistence.Id // JPA ID
    @GeneratedValue(strategy = GenerationType.UUID) // Auto-generate for SQL
    private String id;

    @Column(unique = true, nullable = false)
    private String uniqueLink;

    @ElementCollection // For SQL to store list
    @CollectionTable(name = "collab_doc_content", joinColumns = @JoinColumn(name = "doc_id")) // SQL mapping for list
    private List<CRDTCharacter> content = new ArrayList<>();

    private LocalDateTime createdAt;

    /** ✅ Default Constructor (For JPA) */
    public CollabDoc() {
        this.content = new ArrayList<>();
    }

    /** ✅ Constructor for Creating New Doc */
    public CollabDoc(String uniqueLink) {
        this.uniqueLink = uniqueLink;
        this.content = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    /** ✅ Getter and Setter Methods */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUniqueLink() { return uniqueLink; }
    public void setUniqueLink(String uniqueLink) { this.uniqueLink = uniqueLink; }

    public List<CRDTCharacter> getContent() { return content; }
    public void setContent(List<CRDTCharacter> content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** ✅ Get Full Document Text */
    public String getDocument() {
        StringBuilder result = new StringBuilder();
        for (CRDTCharacter character : content) {
            result.append(character.getValue());
        }
        return result.toString();
    }

    /** ✅ Handle Insert */
    public void handleInsert(String delta, int position, String sessionId) {
        String uniqueId = System.currentTimeMillis() + "_" + sessionId;  // Generate unique ID
        CRDTCharacter newChar = new CRDTCharacter(delta, uniqueId);
        int adjustedPosition = Math.max(0, Math.min(position, this.content.size()));
        this.content.add(adjustedPosition, newChar);  // Insert character
        logger.info("Inserted character '{}' at position {}.", delta, adjustedPosition);
    }

    /** ✅ Handle Delete */
    public void handleDelete(int position) {
        int adjustedPosition = Math.max(0, Math.min(position, this.content.size() - 1));
        if (!this.content.isEmpty() && adjustedPosition < this.content.size()) {
            CRDTCharacter charToDelete = this.content.get(adjustedPosition);
            this.content.remove(adjustedPosition);  // Remove character
            logger.info("Deleted character '{}' at adjusted position {}.", charToDelete.getValue(), adjustedPosition);
        } else {
            logger.warn("Attempted to delete at position {}, but it was out of bounds. Skipping deletion.", position);
        }
    }
}
