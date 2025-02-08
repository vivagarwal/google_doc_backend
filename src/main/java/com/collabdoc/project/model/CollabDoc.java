package com.collabdoc.project.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document
public class CollabDoc {

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
}
