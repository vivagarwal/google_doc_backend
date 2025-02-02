package com.collabdoc.project.controller;


import com.collabdoc.project.model.CollabDoc;
import com.collabdoc.project.service.CollabDocService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/snippets")
public class CollabDocController {

    private final CollabDocService collabDocService;

    public CollabDocController(CollabDocService collabDocService){
        this.collabDocService=collabDocService;
    }

    // POST: Create a new snippet
    @PostMapping("/create")
    public ResponseEntity<CollabDoc> createSnippet(@RequestBody CollabDoc snippet) {
        return ResponseEntity.ok(collabDocService.createSnippet(snippet));
    }

    // GET: Retrieve a snippet using its unique link
    @GetMapping("/view/{uniqueLink}")
    public ResponseEntity<Map<String, String>> getSnippet(@PathVariable String uniqueLink) {
        Optional<CollabDoc> snippet = collabDocService.getSnippet(uniqueLink);

        return snippet
                .map(collabDoc -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("content", collabDoc.getContent());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{uniqueLink}")
    public ResponseEntity<String> updateSnippet(
        @PathVariable String uniqueLink,
        @RequestBody CollabDoc snippetPayload) {

    // System.out.println(uniqueLink);
    // System.out.println(snippetPayload.getContent());
    boolean isUpdated = collabDocService.updateSnippet(uniqueLink, snippetPayload.getContent());

    if (isUpdated) {
        return ResponseEntity.ok("Snippet updated successfully.");
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Snippet not found.");
    }
}
}
