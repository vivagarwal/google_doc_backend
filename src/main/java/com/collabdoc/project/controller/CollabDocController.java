package com.collabdoc.project.controller;

import com.collabdoc.project.model.CollabDoc;
import com.collabdoc.project.service.CollabDocService;
import com.collabdoc.project.manager.InMemoryEditManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/snippets")
public class CollabDocController {

    private final CollabDocService collabDocService;
    private final InMemoryEditManager inMemoryEditManager;

    public CollabDocController(CollabDocService collabDocService, InMemoryEditManager inMemoryEditManager) {
        this.collabDocService = collabDocService;
        this.inMemoryEditManager = inMemoryEditManager;
    }

    // ✅ POST: Create a new snippet
    @PostMapping("/create")
    public ResponseEntity<CollabDoc> createSnippet(@RequestBody CollabDoc snippet) {
        CollabDoc savedDoc = collabDocService.createSnippet(snippet);
        return ResponseEntity.ok(savedDoc);
    }

    // ✅ GET: Retrieve a snippet using its unique link
    @GetMapping("/view/{uniqueLink}")
    public ResponseEntity<Map<String, String>> getSnippet(@PathVariable String uniqueLink) {
        try {
            // Load document into memory
            inMemoryEditManager.loadinMemory(uniqueLink);
            CollabDoc snippet = inMemoryEditManager.viewDoc(uniqueLink);

            if (snippet == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Snippet not found"));
            }

            // Prepare response
            Map<String, String> response = new HashMap<>();
            response.put("content", snippet.getDocument());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error retrieving snippet"));
        }
    }

    // ✅ PUT: Update a snippet using its unique link
    @PutMapping("/update/{uniqueLink}")
    public ResponseEntity<String> updateSnippet(@PathVariable String uniqueLink) {
        try {
            boolean isUpdated = inMemoryEditManager.persistEditsforOne(uniqueLink);

            if (isUpdated) {
                return ResponseEntity.ok("Snippet updated successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Snippet not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update snippet.");
        }
    }
}
