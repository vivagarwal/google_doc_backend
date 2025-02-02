package com.pastebin.project.controller;


import com.pastebin.project.model.PasteBin;
import com.pastebin.project.service.PasteBinService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RestController
@RequestMapping("/api/snippets")
public class PasteBinController {

    private final PasteBinService pasteBinService;

    public PasteBinController(PasteBinService pasteBinService){
        this.pasteBinService=pasteBinService;
    }

    // POST: Create a new snippet
    @PostMapping("/create")
    public ResponseEntity<PasteBin> createSnippet(@RequestBody PasteBin snippet) {
        return ResponseEntity.ok(pasteBinService.createSnippet(snippet));
    }

    // GET: Retrieve a snippet using its unique link
    @GetMapping("/view/{uniqueLink}")
    public ResponseEntity<Map<String, String>> getSnippet(@PathVariable String uniqueLink) {
        Optional<PasteBin> snippet = pasteBinService.getSnippet(uniqueLink);

        return snippet
                .map(pasteBin -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("content", pasteBin.getContent());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{uniqueLink}")
    public ResponseEntity<String> updateSnippet(
        @PathVariable String uniqueLink,
        @RequestBody PasteBin snippetPayload) {

    // System.out.println(uniqueLink);
    // System.out.println(snippetPayload.getContent());
    boolean isUpdated = pasteBinService.updateSnippet(uniqueLink, snippetPayload.getContent());

    if (isUpdated) {
        return ResponseEntity.ok("Snippet updated successfully.");
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Snippet not found.");
    }
}
}
