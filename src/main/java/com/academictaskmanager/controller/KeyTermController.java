package com.academictaskmanager.controller;

import com.academictaskmanager.model.KeyTerm;
import com.academictaskmanager.model.User;
import com.academictaskmanager.service.KeyTermService;
import com.academictaskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST API backing a topic's key-term flashcards, the raw material for generated practice questions. */
@RestController
@RequestMapping("/api/terms")
public class KeyTermController {

    private final KeyTermService keyTermService;
    private final UserService userService;

    public KeyTermController(KeyTermService keyTermService, UserService userService) {
        this.keyTermService = keyTermService;
        this.userService = userService;
    }

    private User currentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }

    @GetMapping
    public List<KeyTerm> byTopic(Authentication authentication, @RequestParam Long topicId) {
        return keyTermService.findByTopic(topicId, currentUser(authentication));
    }

    @PostMapping
    public ResponseEntity<KeyTerm> create(Authentication authentication, @Valid @RequestBody KeyTerm keyTerm) {
        keyTerm.setId(null);
        return ResponseEntity.ok(keyTermService.save(keyTerm, currentUser(authentication)));
    }

    /** Bulk-creates key terms, e.g. the ones a student confirmed from syllabus suggestions. */
    @PostMapping("/bulk")
    public ResponseEntity<List<KeyTerm>> createBulk(Authentication authentication, @RequestBody List<KeyTerm> keyTerms) {
        return ResponseEntity.ok(keyTermService.saveAll(keyTerms, currentUser(authentication)));
    }

    @PutMapping("/{id}")
    public KeyTerm update(Authentication authentication, @PathVariable Long id, @Valid @RequestBody KeyTerm keyTerm) {
        keyTerm.setId(id);
        return keyTermService.save(keyTerm, currentUser(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        keyTermService.delete(id, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }
}
