package com.collabdoc.project.model;

import com.collabdoc.project.manager.InMemoryEditManager;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Entity
@Table(name = "collab_doc")
public class CollabDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUniqueLink() { return uniqueLink; }
    public void setUniqueLink(String uniqueLink) { this.uniqueLink = uniqueLink; }

    public List<CRDTCharacter> getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void handleInsert(String value, int lineNumber, int columnNumber, String sessionId) {
        String uniqueId = System.currentTimeMillis() + "_" + sessionId;
    
        // ✅ If inserting a new line, create an empty line first
    if ("\n".equals(value)) {
        for (CRDTCharacter character : content) {
            if (character.getLineNumber() == lineNumber && character.getColumnNumber()>=columnNumber) {
                character.setLineNumber(character.getLineNumber() + 1);
                character.setColumnNumber(character.getColumnNumber()-columnNumber);
            }else if(character.getLineNumber() > lineNumber){
                character.setLineNumber(character.getLineNumber()+1);
            }
        }
        return;
    }
    else{
    // ✅ Shift characters in the same line after this column
    for (CRDTCharacter character : content) {
        if (character.getLineNumber() == lineNumber && character.getColumnNumber() >= columnNumber) {
            character.setColumnNumber(character.getColumnNumber() + 1);
        }
        }
    }

    CRDTCharacter newChar = new CRDTCharacter(value.trim(), uniqueId, lineNumber, columnNumber);
    newChar.setCollabDoc(this);
    content.add(newChar);
    
        // ✅ Ensure characters remain sorted properly (so database updates correctly)
        content.sort(Comparator.comparingInt(CRDTCharacter::getLineNumber)
                               .thenComparingInt(CRDTCharacter::getColumnNumber));
    }
    

    public void handleDelete(int lineNumber, int columnNumber, String value) {
        Optional<CRDTCharacter> charToDelete = content.stream()
            .filter(c -> c.getLineNumber() == lineNumber && c.getColumnNumber() == columnNumber)
            .findFirst();
            charToDelete.ifPresent(character -> {
                content.remove(character);
            });    
            if ("\n".equals(value)) {
                int newColumnNumber = content.stream()
                    .filter(c -> c.getLineNumber() == lineNumber - 1)
                    .max(Comparator.comparingInt(CRDTCharacter::getColumnNumber))
                    .map(c -> c.getColumnNumber() + 1) // If c1 is found, set col + 1
                    .orElse(0);
                for (CRDTCharacter character : content) {
                    if (character.getLineNumber() == lineNumber && character.getColumnNumber()>=columnNumber) {
                        character.setLineNumber(character.getLineNumber() - 1);
                        character.setColumnNumber(newColumnNumber+columnNumber);
                    }else if(character.getLineNumber() > lineNumber){
                        character.setLineNumber(character.getLineNumber()-1);
                    }
                }
                return;
            }
            else{
            // ✅ Shift characters in the same line after this column
            for (CRDTCharacter character : content) {
                if (character.getLineNumber() == lineNumber && character.getColumnNumber() >= columnNumber) {
                    character.setColumnNumber(character.getColumnNumber() - 1);
                }
                }
            }
    
        // ✅ Ensure characters remain sorted properly
        content.sort(Comparator.comparingInt(CRDTCharacter::getLineNumber)
                               .thenComparingInt(CRDTCharacter::getColumnNumber));
    }
}
