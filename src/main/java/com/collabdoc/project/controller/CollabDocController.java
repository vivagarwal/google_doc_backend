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

    public CollabDocController(CollabDocService collabDocService,InMemoryEditManager inMemoryEditManager){
        this.collabDocService=collabDocService;
        this.inMemoryEditManager=inMemoryEditManager;
    }

    // POST: Create a new snippet
    @PostMapping("/create")
    public ResponseEntity<CollabDoc> createSnippet(@RequestBody CollabDoc snippet) {
        return ResponseEntity.ok(collabDocService.createSnippet(snippet));
    }

    // GET: Retrieve a snippet using its unique link
    @GetMapping("/view/{uniqueLink}")
    public ResponseEntity<Map<String, String>> getSnippet(@PathVariable String uniqueLink) {
        try{
            inMemoryEditManager.loadinMemory(uniqueLink);
            CollabDoc snippet = inMemoryEditManager.viewDoc(uniqueLink);
            Map<String, String> response = new HashMap<>();
            response.put("content", snippet.getDocument());
            System.out.println(response.get("content"));
            return ResponseEntity.ok(response);

        }
        catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    //PUT : Update a snippet using its unique link
    @PutMapping("/update/{uniqueLink}")
    public ResponseEntity<String> updateSnippet(@PathVariable String uniqueLink) {
    // System.out.println(snippetPayload.getContent());
    boolean isUpdated = inMemoryEditManager.persistEditsforOne(uniqueLink);
    if (isUpdated) {
        return ResponseEntity.ok("Snippet updated successfully.");
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Snippet not found.");
        }
    }
}