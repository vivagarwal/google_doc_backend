package com.collabdoc.project.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "crdt_character")
public class CRDTCharacter {

    @Id
    private String uniqueId;  // Unique identifier

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private int lineNumber;  // 🔥 Stores the line in the document

    @Column(nullable = false)
    private int columnNumber; // 🔥 Stores the column position

    @ManyToOne
    @JoinColumn(name = "collab_doc_id", nullable = false) 
    @JsonBackReference
    private CollabDoc collabDoc;

    public CRDTCharacter(String value, String uniqueId, int lineNumber, int columnNumber) {
        this.value = value;
        this.uniqueId = uniqueId;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public CRDTCharacter() {}

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

    public int getColumnNumber() { return columnNumber; }
    public void setColumnNumber(int columnNumber) { this.columnNumber = columnNumber; }

    public CollabDoc getCollabDoc() { return collabDoc; }
    public void setCollabDoc(CollabDoc collabDoc) { this.collabDoc = collabDoc; }

    @Override
    public String toString() {
        return "[" + lineNumber + "," + columnNumber + "] -> " + value;
    }
}
