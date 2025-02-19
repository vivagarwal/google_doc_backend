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

    @Transient  // Not persisted in DB
    private List<String> deletedCharacters = new ArrayList<>();  // Holds deleted characters

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

    // this is not needed now as we are modifying it and never setting it
    // public void setContent(List<CRDTCharacter> content) {
    //     this.content.clear();
    //     for (CRDTCharacter character : content) {
    //         character.setCollabDoc(this); // Set Foreign Key
    //     }
    //     this.content.addAll(content);
    // }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void handleInsert(String delta, int position, String sessionId) {
        String uniqueId = System.currentTimeMillis() + "_" + sessionId;
        int adjustedPosition = Math.max(0, Math.min(position, content.size()));

        // Shift sequence numbers of all characters after this position
        for(CRDTCharacter oldChar: content)
        {
            if(oldChar.getSequence() >= adjustedPosition)
            {
                oldChar.setSequence(oldChar.getSequence()+1);
            }
        }

        CRDTCharacter newChar = new CRDTCharacter(delta, uniqueId, adjustedPosition);
        newChar.setCollabDoc(this); // Set foreign key reference
        content.add(adjustedPosition, newChar);
        logger.info("Inserted character '{}' at position {}.", delta, position);
    }

    public void handleDelete(int position) {
        int adjustedPosition = Math.max(0, Math.min(position, content.size() - 1));
        if (content.size() > 0 && adjustedPosition < content.size()) {
            CRDTCharacter charToDelete = content.get(adjustedPosition);
            deletedCharacters.add(charToDelete.getUniqueId());  // Collect deleted characters
            content.remove(adjustedPosition);

            // Shift sequence numbers of all characters after the deleted one
            for(CRDTCharacter oldchar: content)
            {
                if(oldchar.getSequence() > adjustedPosition)
                {
                    oldchar.setSequence(oldchar.getSequence()-1);
                }
            }
            logger.info("Marked character '{}' at position {} for deletion.", charToDelete.getValue(), adjustedPosition);
        } else {
            logger.warn("Attempted to delete at position {}, but it was out of bounds. Skipping deletion.", position);
        }
    }

    public List<String> getDeletedCharacters() {
        return deletedCharacters;
    }
}
