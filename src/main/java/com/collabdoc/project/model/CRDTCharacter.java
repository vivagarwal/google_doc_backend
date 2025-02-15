package com.collabdoc.project.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "crdt_character")
public class CRDTCharacter {

    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY) // Ensure ID is Long
    // private Long id;
    // @Column(nullable = false, unique = true)
    private String uniqueId;  // Unique identifier

    @Column(nullable = false)
    private String value;

    @ManyToOne
    @JoinColumn(name = "collab_doc_id", nullable = false) // Foreign key reference
    @JsonBackReference
    private CollabDoc collabDoc;

    public CRDTCharacter(String value, String uniqueId) {
        this.value = value;
        this.uniqueId = uniqueId;
    }

    public CRDTCharacter() {}

    // public Long getId() {
    //     return id;
    // }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getValue() {
        return value;
    }

    public CollabDoc getCollabDoc() {
        return collabDoc;
    }

    public void setCollabDoc(CollabDoc collabDoc) {
        this.collabDoc = collabDoc;
    }

    @Override
    public String toString() {
        return "" + value;
    }
}
