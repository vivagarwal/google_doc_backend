package com.collabdoc.project.model;

import com.collabdoc.project.manager.CollabDocState;
import com.collabdoc.project.manager.InMemoryEditManager;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;


@Entity
@Table(name = "collab_doc")
public class CollabDoc {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryEditManager.class);

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
                    if (character.getLineNumber() == lineNumber) {
                        character.setLineNumber(character.getLineNumber() - 1);
                        character.setColumnNumber(newColumnNumber+character.getColumnNumber());
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

    public static List<String> viewOrderedDoc(String uniqueLink) {
        CollabDocState collabDocState = InMemoryEditManager.inMemoryEdits.get(uniqueLink);
    
        if (collabDocState == null) {
            logger.error("Document '{}' not found in memory. Returning an empty list.", uniqueLink);
            return new ArrayList<>();
        }
    
        CollabDoc document = collabDocState.getCollabDoc();
        // ✅ Preserve spaces while reconstructing the document
       Map<Integer, Map<Integer, String>> structuredLines = document.getContent().stream()
    .sorted(Comparator
        .comparingInt(CRDTCharacter::getLineNumber)
        .thenComparingInt(CRDTCharacter::getColumnNumber))
    .collect(Collectors.groupingBy(
        CRDTCharacter::getLineNumber,
        LinkedHashMap::new,
        Collectors.toMap(
            CRDTCharacter::getColumnNumber,
            CRDTCharacter::getValue,
            (existing, replacement) -> existing,
            LinkedHashMap::new
        )
    ));

// Determine the min and max line numbers
int minLine = structuredLines.keySet().stream().min(Integer::compareTo).orElse(0);
int maxLine = structuredLines.keySet().stream().max(Integer::compareTo).orElse(-1);

List<String> reconstructedLines = new ArrayList<>();

// Loop through every line from minLine to maxLine
for (int lineNum = minLine; lineNum <= maxLine; lineNum++) {

  Map<Integer, String> lineMap = structuredLines.get(lineNum);
  if (lineMap == null) {
    // If no entry for this line, produce an empty line
    reconstructedLines.add("");
    continue;
  }

  // Otherwise build it the same as before
  int maxColumn = lineMap.keySet().stream().max(Integer::compareTo).orElse(0);
  StringBuilder lineBuilder = new StringBuilder();

  // Pre-fill with spaces
  for (int i = 0; i <= maxColumn; i++) {
    lineBuilder.append(' ');
  }

  // Place each character
  lineMap.forEach((column, value) -> {
    if (value != null && !value.isEmpty() && column < lineBuilder.length()) {
      lineBuilder.setCharAt(column, value.charAt(0));
    }
  });

  reconstructedLines.add(lineBuilder.toString());
}

return reconstructedLines;
    }
}
