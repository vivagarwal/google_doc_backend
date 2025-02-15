package com.collabdoc.project.model;

import com.collabdoc.project.manager.InMemoryEditManager;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Entity
@Table(name = "collab_doc")
public class CollabDoc {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryEditManager.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    @Column(unique = true, nullable = false)
    private String uniqueLink;

    @OneToMany(mappedBy = "collabDoc", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<CRDTCharacter> content = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public CollabDoc(String uniqueLink) {
        this.uniqueLink = uniqueLink;
        this.content = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public CollabDoc() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueLink() {
        return uniqueLink;
    }

    public void setUniqueLink(String uniqueLink) {
        this.uniqueLink = uniqueLink;
    }

    public List<CRDTCharacter> getContent() {
        return content;
    }

    public void setContent(List<CRDTCharacter> content) {
        this.content.clear();
        for (CRDTCharacter character : content) {
            character.setCollabDoc(this); // Set Foreign Key
        }
        this.content.addAll(content);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDocument() {
        StringBuilder result = new StringBuilder();
        for (CRDTCharacter character : content) {
            result.append(character.getValue());
        }
        return result.toString();
    }

    public void handleInsert(String delta, int position, String sessionId) {
        String uniqueId = System.currentTimeMillis() + "_" + sessionId;
        CRDTCharacter newChar = new CRDTCharacter(delta, uniqueId);
        newChar.setCollabDoc(this); // Set foreign key reference
        int adjustedPosition = Math.max(0, Math.min(position, this.getContent().size()));
        this.getContent().add(adjustedPosition, newChar);
        logger.info("Inserted character '{}' at position {}.", delta, position);
    }

    public void handleDelete(int position) {
        int adjustedPosition = Math.max(0, Math.min(position, this.getContent().size() - 1));
        if (this.getContent().size() > 0 && adjustedPosition < this.getContent().size()) {
            CRDTCharacter charToDelete = this.getContent().get(adjustedPosition);
            this.getContent().remove(adjustedPosition);
            logger.info("Deleted character '{}' at adjusted position {}.", charToDelete.getValue(), adjustedPosition);
        } else {
            logger.warn("Attempted to delete at position {}, but it was out of bounds. Skipping deletion.", position);
        }
    }
}
