package com.collabdoc.project.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.collabdoc.project.manager.InMemoryEditManager;

import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Document
public class CollabDoc {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryEditManager.class);

    @Id
    private String id;

    private String uniqueLink;

    private List<CRDTCharacter> content;

    private LocalDateTime createdAt;

    public CollabDoc(String uniqueLink){
        this.uniqueLink=uniqueLink;
        this.content=new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUniqueLink() {
        return uniqueLink;
    }

    public void setUniqueLink(String uniqueLink) {
        this.uniqueLink = uniqueLink;
    }

    public List<CRDTCharacter> getContent(){
        return content;
    }

    public void setContent(List<CRDTCharacter> content){
        this.content=content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDocument(){
        StringBuilder result=new StringBuilder();
        for(CRDTCharacter character : content){
                result.append(character.getValue());
        }
        return result.toString();
    }

    public void handleInsert(String delta, int position, String sessionId) {
        String uniqueId = System.currentTimeMillis() + "_" + sessionId;  // Generate unique ID
        CRDTCharacter newChar = new CRDTCharacter(delta, uniqueId);
        int adjustedPosition = Math.max(0, Math.min(position, this.getContent().size()));
        this.getContent().add(adjustedPosition, newChar);  // Insert character
        logger.info("Inserted character '{}' at position {}.", delta, position);
    }

    // Handle deletion by marking the character as logically deleted
    public void handleDelete(int position) {
    // Adjust the position to ensure it's within bounds
    int adjustedPosition = Math.max(0, Math.min(position, this.getContent().size() - 1));
    if (this.getContent().size() > 0 && adjustedPosition < this.getContent().size()) {
        CRDTCharacter charToDelete = this.getContent().get(adjustedPosition);
        this.getContent().remove(adjustedPosition);  // Remove character from list
        logger.info("Deleted character '{}' at adjusted position {}.", charToDelete.getValue(), adjustedPosition);
    } else {
        logger.warn("Attempted to delete at position {}, but it was out of bounds. Skipping deletion.", position);
    }
    }
}
