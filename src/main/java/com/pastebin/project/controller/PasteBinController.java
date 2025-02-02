package com.pastebin.project.controller;


import com.pastebin.project.model.PasteBin;
import com.pastebin.project.service.PasteBinService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


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
}
