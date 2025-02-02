package com.collabdoc.project.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Document
public class CollabDoc {

    @Id
    private String id;

    private String content;

    private LocalDateTime createdAt;

    private String uniqueLink;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUniqueLink() {
        return uniqueLink;
    }

    public void setUniqueLink(String uniqueLink) {
        this.uniqueLink = uniqueLink;
    }
}
